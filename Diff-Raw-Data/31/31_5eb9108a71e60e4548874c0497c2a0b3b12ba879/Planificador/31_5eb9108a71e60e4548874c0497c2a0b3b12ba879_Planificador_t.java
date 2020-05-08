 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  * 
  * Simula el sistema de computacion
  */
 package Funcionalidad;
 
 import java.util.ArrayList;
 import Constantes.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * @author Hector Dominguez @version 1.0
  * @author Gustavo Ortega & Carlos Aponteector @version 1.1
  */
 public class Planificador extends Thread {
 
     Cpu cpu;
     ArrayList<Proceso> listaFinalizados;
     DispositivoIO dispositivoIO;
     Reloj reloj;
     Boolean terminoSimulacion = false;
     double tiempoEsperaProm ;
     double tiempoDormidoProm;
 
     public Boolean getTerminoSimulacion() {
         return terminoSimulacion;
     }
 
     public Planificador(String NombreArch, Reloj reloj) {
         // Tiene todos los procesos del archivo procesos.xml
         ArrayList<Proceso>[] procesos = LectorXML.obtenerProcesos(NombreArch);
        System.out.println("*****");
        for (int i = 0; i < procesos.length; i++) {
            System.out.println(i +": "+procesos[i].toString());
        }
        System.out.println("*****");
 
         // Crea 140 listas: 1 para c/prioridad.
         ListasDePrioridades listasActivos = new ListasDePrioridades(procesos[0]);
         ListasDePrioridades listasExpirados = new ListasDePrioridades();
         
         // Crea "cola de listos"
         Runqueue colaListos = new Runqueue(listasActivos, listasExpirados);
 
         // Asocia al CPU su runqueue: es una por c/CPU.
         cpu = new Cpu((short) 1, colaListos, procesos);
         cpu.setPlanificador(this);
         cpu.setName("Cpu");
 
         dispositivoIO = new DispositivoIO();
         dispositivoIO.setPlanificador(this);
 
         listaFinalizados = new ArrayList<Proceso>();
 
         this.reloj = reloj;
     }
 
 // ========================     Getters/Setters         ========================
 
     public Reloj getReloj() {
         return reloj;
     }
 
     public Cpu getCpu() {
         return cpu;
     }
 
     public void setCpu(Cpu cpu) {
         this.cpu = cpu;
     }
 
     public ArrayList<Proceso> getListaFinalizados() {
         return listaFinalizados;
     }
 
     public void setListaFinalizados(ArrayList<Proceso> listaFinalizados) {
         this.listaFinalizados = listaFinalizados;
     }
 
     public DispositivoIO getDispositivoIO() {
         return dispositivoIO;
     }
 
     public void setDispositivoIO(DispositivoIO dispositivoIO) {
         this.dispositivoIO = dispositivoIO;
     }
 // ========================     FIN Getters/Setters     ========================
 
     /* Mueve el proceso p a la cola de expirados*/
     public void insertarListaExpirados(Proceso p) {
         boolean b = cpu.getRunqueue().insertarProcesoExpirado(p);
     }
 
     public boolean insertarListaFinalizados(Proceso p) {
         boolean agrego = false;
         if (listaFinalizados.indexOf(p) == -1) {
             p.setEstado(Constantes.EXIT_DEAD);
             agrego = this.listaFinalizados.add(p);
         }
         return agrego;
     }
 
     public int obtenerBono(Proceso p) {
         return p.getTiempoDurmiendo() / 100;
     }
 
     public int actualizarPrioridadDinamica(Proceso p) {
         int prioridadDinamica = java.lang.Math.max(100,
                 java.lang.Math.min(p.getPrioridadEstatica() - this.obtenerBono(p) + 5, 139));
 
         p.setPrioridadDinamica(prioridadDinamica);
         return prioridadDinamica;
     }
 
     // Funcion: scheduler_tick()                                           *****
     public void actualizarQuantum(Proceso p) {
         Boolean asigno = false;
         // OJO: 4: adquiere lock de runqueue
         // 5:
         int prioridadEstatica = p.getPrioridadEstatica();
         int quantum = p.getQuantum();
         int tcpu = p.getTiemposCPU();
 
         // Siempre es eliminado el proceso de Activos
         p.setQuantum(--quantum);
         // Disminuye num de Ticks que necesita para terminar ejecucion
         p.setTiemposCPU(--tcpu);
         // OJO: HAY que decrementar p.sleep_avg en Reloj.tick, WHY???
         p.decrementarTiempoDurmiendo();
 
         if (quantum <= 0) {
 
             // set_tsk_need_resched(current):
             cpu.setProcesoActual(null);
             cpu.getRunqueue().setProcesoActual(null);
             // FIN set_tsk_need_resched(current):
 
             // Si todavia le queda trabajo al proceso
             if (tcpu > 0) {
 
                 quantum = (140 - prioridadEstatica) * (prioridadEstatica < 120 ? 20 : 5);
                 p.setQuantum(java.lang.Math.min(quantum, tcpu));
                 p.setEsPrimerQuantum(false);
 
                 if (p.esTiempoReal()) { // Suponemos que son Round Robin                
                     // Se inserta a activos:
                     cpu.getRunqueue().getActivos().insertarProceso(p);
                 } else {
                     this.actualizarPrioridadDinamica(p);
 
                     if (cpu.getRunqueue().getTiempoPrimerExpirado() == 0) {
                         cpu.getRunqueue().setTiempoPrimerExpirado(reloj.getNumTicks());
                     }
                     // f: Asumimos que siempre añadiremos a expirados
                     cpu.getRunqueue().insertarProcesoExpirado(p);
 
                 }
 
                 /*p.getTiemposIO() devuelve un entero si es -1 es que no hay 
                  * lista de tiempos para entrada salida*/
             } else if (p.getTiemposIO() != 0 && p.getTiemposIO() != -1) {
                 // Mandar proceso actual a la cola IO
                 if (dispositivoIO.insertarColaBloqueados(p)) {
                     System.out.println("Insertado en cola de dispositivo");
                     System.out.println(dispositivoIO.getColaBloqueados().toString());
                 } else {
                     System.out.println("NO insertado");
                 }
             } else {
                 insertarListaFinalizados(p);
             }
             asigno = this.asignarCPU(); // OJO
         }
     }
 
     // Se obtiene la cantidad de procesos que fueron al dispositivo.
     public int cantProcesosIO() {
         int cant = 0;
         for (int i = 0; i < listaFinalizados.size(); i++) {
             if (listaFinalizados.get(i).getTiempoTotalDurmiendo() > 0) {
                 cant++;
             }
         }
         return cant;
     }
 
     //Funciones que busca los procesos que vinieron de IO para ponerlos activos.
     public void BuscaDespierta() {
         ArrayList<Proceso>[] expirados = this.getCpu().getRunqueue().getExpirados().getListas();
         for (int i = 0; i < expirados.length; i++) {
             boolean a = expirados[i].isEmpty();
             if (!expirados[i].isEmpty()) {
                 for (int j = 0; j < expirados[i].size(); j++) {
                     if (expirados[i].get(j).getEstado() == Constantes.TASK_INTERRUPTIBLE) {
                         this.despiertaProceso(expirados[i].get(j));
                     }
                 }
             }
         }
     }
 
     //Busca si hay algun proceso que haya terminado su tiempo de IO//
     public boolean hayDormido() {
         ArrayList<Proceso>[] expirados = this.getCpu().getRunqueue().getExpirados().getListas();
         for (int i = 0; i < expirados.length; i++) {
             if (expirados[i].size() >= 0) {
                 for (int j = 0; j < expirados[i].size(); j++) {
                     if (expirados[i].get(j).getEstado() == Constantes.TASK_INTERRUPTIBLE) {
                         return true;
                     }
 
                 }
             }
         }
         return false;
 
     }
 
     // Intenta despuertar un proceso que esta en el dispositivo.
     public boolean despiertaProceso(Proceso p) {
         boolean despertado = false;
         int prioridadDinamica;
         int quantum = (140 - p.getPrioridadEstatica()) * (p.getPrioridadEstatica() < 120 ? 20 : 5);
 
         if (p.getTiemposCPU() == 0) {
             insertarListaFinalizados(p);
 
             if (listaFinalizados.size() == cpu.getTotalProcesos()) {
                 terminarSimulacion(cpu.getTiempoOcioso(), reloj.getNumTicks());
             }
 
             System.out.println("FINALIZADOS: " + listaFinalizados.size());
             System.out.println(listaFinalizados.toString());
         } else {
             p.setQuantum(java.lang.Math.min(quantum, p.getTiemposCPU()));
 
             if (p.getEstado() == Constantes.TASK_INTERRUPTIBLE) {
                 prioridadDinamica = this.reCalcularPrioridadDinamica(p, reloj.getNumTicks());
                 p.setEstado(Constantes.TASK_RUNNING);
                 // Si CPU esta ocioso (porque runqueue esta vacia)
                 if (cpu.getProcesoActual() == null) {
                     cpu.setProcesoActual(p);
                     cpu.getRunqueue().setProcesoActual(p);
                 } else {
                     cpu.getRunqueue().insertarProcesoActivo(p);
                     // Se verifica si tiene mayor o menor prioridad que el que esta en CPU
                     if (cpu.getProcesoActual().getPrioridadDinamica() > prioridadDinamica) {
                         cpu.getRunqueue().insertarProcesoActivo(cpu.getProcesoActual());
                         cpu.setProcesoActual(null);
                         cpu.getRunqueue().setProcesoActual(null);
                         this.asignarCPU();
                     }
                 }
                 despertado = true;
             }
         }
         return despertado;
     }
 
    // Se recalcula la prioridad dinamica del proceso que viene de IO.
     public int reCalcularPrioridadDinamica(Proceso p, int numTicks) {
         int prioridadDinamica = this.actualizarPrioridadDinamica(p);
         return prioridadDinamica;
     }
 
    // Le asigna el CPU al "mejor" proceso de la cola del CPU, si no hay 
    // activos hace swap con la cola de expirados.
     public boolean asignarCPU() {
         System.out.println("RUNQUEUE");
         System.out.println(cpu.getRunqueue().toString());
 
         Proceso p = cpu.getRunqueue().obtenerMejorProceso();
 
         if (p != null) {
             cpu.getRunqueue().eliminarProcesoActivo(p);
             cpu.setProcesoActual(p);
             cpu.getRunqueue().setProcesoActual(p);
         } else {
             cpu.getRunqueue().intercambioActivosExpirados();
             p = cpu.getRunqueue().obtenerMejorProceso();
 
             if (p != null) {
                 cpu.getRunqueue().eliminarProcesoActivo(p);
                 cpu.setProcesoActual(p);
                 cpu.getRunqueue().setProcesoActual(p);
             }
         }
 
        if (p == null) {
             System.out.println("RUNQUEUE VACIA!!");
             System.out.println("FINALIZADOS: " + listaFinalizados.size());
             System.out.println(listaFinalizados.toString());
 
             if (listaFinalizados.size() == cpu.getTotalProcesos()) {
                 terminarSimulacion(cpu.getTiempoOcioso(), reloj.getNumTicks());
             }
 
             return false;
         } else {
             cpu.getRunqueue().aumentarNumProcesosCambiados();
             return true;
         }
     }
 
     //Agrega un nuevo proceso a la cola de activos.
     public void agregarNuevoProceso(Proceso p) {
 
         int prioridadDinamica = p.getPrioridadDinamica();
         // Si CPU esta ocioso (porque runqueue esta vacia)   
         if (cpu.getProcesoActual() == null) {
             cpu.setProcesoActual(p);
             cpu.getRunqueue().setProcesoActual(p);
         } else {
             cpu.getRunqueue().insertarProcesoActivo(p);
             // Se verifica si tiene mayor o menor prioridad que el que esta en CPU
             if (cpu.getProcesoActual().getPrioridadDinamica() > prioridadDinamica) {
                 cpu.getRunqueue().insertarProcesoActivo(cpu.getProcesoActual());
                 cpu.setProcesoActual(null);
                 cpu.getRunqueue().setProcesoActual(null);
                 this.asignarCPU();
             }
         }
     }
 
     @Override
     public void run() {
         super.run();
     }
 
     @Override
     // Vuelve un String la informacion del planificador.
     public String toString() {
         return "Planificador{\n" + "\tcpu=" + cpu + ",\n}";
     }
 
     // Finaliza la simulacion una vez que todos los procesos terminaron, termina
     // a los hilos y calcula estadisticas.
     private void terminarSimulacion(int tiempoOciosoCPU, int numTicksReloj) {
         double tiempoEsperaPromedio = 0.0, tiempoDormidoPromedio = 0.0;
         int cant = cantProcesosIO();
         //cpu.setSimulacion(false);
         reloj.setSimulacion(false);
 
         for (int i = 0; i < listaFinalizados.size(); i++) {
             tiempoEsperaPromedio += listaFinalizados.get(i).getTiempoEsperando();
             tiempoDormidoPromedio += listaFinalizados.get(i).getTiempoTotalDurmiendo();
         }
 
         tiempoEsperaProm = tiempoEsperaPromedio / listaFinalizados.size();
         tiempoDormidoProm = 0.0;
         
         System.out.println("***************************************************");
         System.out.println("FINALIZO SIMULACION");
         System.out.println("Num Ticks Ocioso el CPU: " + tiempoOciosoCPU);
         System.out.println("Num Ticks que Conto el Reloj (tiempo que duro la simulacion): " + numTicksReloj);
         System.out.println("Ticks de Espera Promedio: " + tiempoEsperaProm);
         
         
                 
         if (cant > 0) {
             tiempoDormidoProm = tiempoDormidoPromedio/cant;
             System.out.println("Ticks de Dormido Promedio: " + tiempoDormidoProm);
             
         } else {
             System.out.println("Ticks de Dormido Promedio: 0.0");
         }
         System.out.println("***************************************************");
         terminoSimulacion = true;
     }
 }
