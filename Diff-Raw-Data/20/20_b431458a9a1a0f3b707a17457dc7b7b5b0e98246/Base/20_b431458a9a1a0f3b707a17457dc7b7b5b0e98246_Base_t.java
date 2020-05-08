 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package hourcalendar;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JFrame;
 import javax.swing.SwingUtilities;
 
 /**
  *
  * @author asus
  */
 public class Base {
     public Vector<Assignatura> assignatures;
     private Vector<Grau> graus;
     //public Vector<TipusHoresPractica> tipusHoresPractiques;
     public Vector<TipusMateria> tipusMateries;
     public Vector<TipusAula> tipusAules;
     public Vector<Aula> aules;
     private List<Date> diesDocencia;
     private Vector setmanaOrdreQ1;
     private Vector setmanaOrdreQ2;
     public Vector<DisponibilitatHoraria> disponibilitatsHoraries;
     public Vector<String> nomDisponibilitatsHoraries;
     public Vector<Color> colorDisponibilitatsHoraries;
     public int progres;
     public String nomProgres = "Inicialitzant...";
     public Generador generador;
     public String informe = "";
     
     public int idTipusHoresPractica = 0;
     public int idTipusMateria = 0;
     public int idTipusAula = 0;
     public int idAula = 0;
     public int idGrau = 0;
     
     public Base(int conjuntDeDades) {
         //loadDiesDocencia();
         setmanaOrdreQ1 = new Vector<Integer>();
         setmanaOrdreQ2 = new Vector<Integer>();
         ModsCalendari.load();
         assignatures = new Vector<Assignatura>();
         graus = new Vector<Grau>();
         //tipusHoresPractiques = new Vector<TipusHoresPractica>();
         tipusMateries = new Vector<TipusMateria>();
         tipusAules = new Vector<TipusAula>();
         aules = new Vector<Aula>();
         
         //si conjuntDeDades == 0 utilitzem les dades de l'última sessió, per tant, no canviem res
         String directori = "";
         if (conjuntDeDades != 0) {
             directori = (conjuntDeDades == 1) ? "dades\\default\\" : "dades\\empty\\";
         }
         loadState(directori);
         //updateDisponibilitatHoraria(1, 2012);
         /*addGrau("Informàtica", "01", "I");
         addGrau("Enginyeria Mecànica", "02", "N");
         addGrau("Disseny Industrial", "03", "D");
         addTipusAula("Aula");
         addTipusAula("Laboratori d'informàtica");
         addTipusAula("Laboratori de mecànica");
         addTipusAula("Laboratori de física");
         addAula("AA205", 60, getTipusAula(0));
         addTipusMateria("Matèries Bàsiques");
         addTipusMateria("Matèries comuns d'àmbit");
         addTipusMateria("Matèries d'especialitat");
         
         
         addAssignatura(getGrauAt(0), "PROJECTE DE PROGRAMACIÓ", "PROP", 340380, getTipusMateria(0),
                 25, 4, 30, 30, getTipusAula(0), 2);
         addAssignatura(getGrauAt(0), "XARXES DE COMPUTADORS", "XACO", 340356, getTipusMateria(0),
                 25, 4, 42, 18, getTipusAula(0), 3);
         addAssignatura(getGrauAt(0), "ESTRUCTURA DE LA INFORMACIÓ", "ESIN", 300000, getTipusMateria(0),
                 25, 2, 30, 30, getTipusAula(0), 8);
         addAssignatura(getGrauAt(0), "XARXES MULTIMÈDIA", "XAMU", 370251, getTipusMateria(0),
                 25, 2, 42, 18, getTipusAula(0), 3);
         addAssignatura(getGrauAt(0), "EMPRESA", "EMPR", 670200, getTipusMateria(0),
                 25, 2, 60, 0, getTipusAula(0), 0);
         addAssignatura(getGrauAt(1), "SOSTENIBILITAT", "SOST", 370001, getTipusMateria(0),
                 25, 2, 42, 18, getTipusAula(0), 3);
         addAssignatura(getGrauAt(1), "PROJECTE DE TECNOLOGIES DE LA INFORMACIÓ", "PTIN", 555555, getTipusMateria(0),
                 25, 2, 42, 18, getTipusAula(0), 3);
         addAssignatura(getGrauAt(2), "GESTIÓ DE PROJECTES", "GEPR", 340037, getTipusMateria(0),
                 25, 4, 30, 30, getTipusAula(0), 3);
         addAssignatura(getGrauAt(2), "DISSENY I REPRESENTACIÓ TÈCNICA", "DIRT", 340075, getTipusMateria(0),
                 25, 4, 42, 18, getTipusAula(0), 3);
         
         */
     }
     
     public int getNumAssignatures() {
         return assignatures.size();
     }
     
     /** Retorna una assignatura donada la seva posició dins el vector. **/
     public Assignatura getAssignaturaAt(int idAssignatura) {
         return assignatures.get(idAssignatura);
     }
     
     /** Retorna una assignatura donat el seu codi, null si no existeix. */
     public Assignatura getAssignatura(int codi) {
         
         int numAssignatures = getNumAssignatures();
         for (int i = 0; i < numAssignatures; ++i) {
             Assignatura assignatura = assignatures.get(i);
             if (assignatura.getCodi() == codi) {
                 return assignatura;
             }
         }
         return null;
     }
     
     public void removeAssignatura(Assignatura assignatura) {
         assignatures.removeElement(assignatura);
         saveState();
     }
     
     public int getIndexOfAssignatura(Assignatura assignatura) {
         return assignatures.indexOf(assignatura);
     }
     
     /** Afegeix o modifica una nova assignatura segons el codi d'assignatura.
      * @return boolean indica si s'ha afegit l'assignatura correctament, no s'afegirà si algun dels paràmetres no és l'esperat
      **/
     public boolean addAssignatura(Grau _grau, String _nom, String _sigles, int _codi, TipusMateria _tipus, int _alumnes, int _quadrimestre, int _horesTeoria, int _horesPractica, TipusAula _tipusHoresPractica, int _grups) {
         
         Assignatura assignatura = new Assignatura(_grau, _nom, _sigles, _codi, _tipus, _alumnes, _quadrimestre, _horesTeoria, _horesPractica, _tipusHoresPractica, _grups);
         
         if (assignatura.esValida()) {
             Assignatura aux = getAssignatura(_codi);
             if (aux != null) {
                 //L'assignatura ja existia, la modifiquem
                 int indexExistent = getIndexOfAssignatura(aux);
                 assignatures.setElementAt(assignatura, indexExistent);
                 aux = null;//assignatura;
             } else {
                 //Afegint una nova assignatura
                 assignatures.add(assignatura);
             }
             saveState();
             return true;
         } else {
             return false;
         }
     }
     
     
     public int getNumGraus() {
         return graus.size();
     }
     
     public Grau getGrauAt(int indexGrau) {
         return graus.get(indexGrau);
     }
     
     public Grau getGrau(int id) {
         int numGraus = getNumGraus();
         for (int i = 0; i < numGraus; ++i) {
             Grau grau = graus.get(i);
             if (grau.getID() == id) {
                 return grau;
             }
         }
         return null; 
     }
     
     public void addGrau(String nom, String codi, String inicial) {
         Grau grau = new Grau(idGrau++, nom, codi, inicial);
         graus.add(grau);
         saveState();
     }
     
     public void removeGrau(Grau grau) {
         graus.removeElement(grau);
         saveState();
     }
     
     public void addTipusMateria(String tipus) {
         
         int id = idTipusMateria++;
         TipusMateria tipusmateria = new TipusMateria(id, tipus);
         tipusMateries.add(tipusmateria);
         saveState();
     }
     
     public int getNumTipusMateries() {
         return tipusMateries.size();
     }
     
     public TipusMateria getTipusMateria(int idTipus) {
         int numTipusMateries = getNumTipusMateries();
         for (int i = 0; i < numTipusMateries; ++i) {
             TipusMateria tipusMateria = tipusMateries.get(i);
             if (tipusMateria.getID() == idTipus) {
                 return tipusMateria;
             }
         }
         return null; 
     }
     
     public TipusMateria getTipusMateriaAt(int index) {
         return tipusMateries.get(index);
     }
     
     public void removeTipusMateria(TipusMateria tipusMateria) {
         tipusMateries.removeElement(tipusMateria);
         saveState();
     }
     
     public int getIndexOfTipusMateria(TipusMateria materia) {
         return tipusMateries.indexOf(materia);
     }
     
     /*public void addTipusHoresPractica(String tipus) {
         tipusHoresPractiques.add(new TipusHoresPractica(idTipusHoresPractica++, tipus));
         saveState();
     }*/
     
     /*public int getNumTipusHoresPractiques() {
         return tipusHoresPractiques.size();
     }*/
     
     /*public TipusHoresPractica getTipusHoresPractica(int idTipus) {
         int numTipusHoresPractiques = getNumTipusHoresPractiques();
         for (int i = 0; i < numTipusHoresPractiques; ++i) {
             TipusHoresPractica tipusHoresPractica = tipusHoresPractiques.get(i);
             if (tipusHoresPractica.getID() == idTipus) {
                 return tipusHoresPractica;
             }
         }
         return null; 
     }*/
     
     public void addTipusAula(String tipus) {
         TipusAula tipusaula = new TipusAula(idTipusAula++, tipus);
         tipusAules.add(tipusaula);
         saveState();
     }
     
     public void removeTipusAula(TipusAula tipus) {
         tipusAules.removeElement(tipus);
         saveState();
     }
     
     public void removeAllTipusAula() {
         if (!tipusAules.isEmpty()) {
             System.out.println("BORRANT");
             tipusAules.clear();
             idTipusMateria = 0;
             saveState();
             System.out.println("BORRAT");
         }
     }
     
     public int getNumTipusAules() {
         //return idTipusAula; NOTA: Si s'eliminen aules, al iterar el vector tipusAules faria saltar una excepció ja que intentaria accedir a una posició del vector inexistent
         return tipusAules.size();
     }
     
     public TipusAula getTipusAula(int idTipus) {
         int numTipusAules = getNumTipusAules();
         for (int i = 0; i < numTipusAules; ++i) {
             TipusAula tipusaula = tipusAules.get(i);
             if (tipusaula.getID() == idTipus) {
                 return tipusaula;
             }
         }
         return null; 
     }
     
     public int getIndexOfTipusAula(TipusAula aula) {
         return tipusAules.indexOf(aula);
     }
     
     public TipusAula getTipusAulaAt(int index) {
         return tipusAules.get(index);
     }
     
     public TipusAula getTipusAula(String nomTipus) {
         int numTipusAules = getNumTipusAules();
         for (int i = 0; i < numTipusAules; ++i) {
             TipusAula tipusaula = tipusAules.get(i);
             if (tipusaula.getNom().equals(nomTipus)) {
                 return tipusaula;
             }
         }
         return null; 
     }
     
     public void addAula(String nom, int capacitat, TipusAula tipusAula) {
         //int id = idAula++;
         Aula aula = new Aula(idAula++, nom, capacitat, tipusAula);
         aules.add(aula);
         saveState();
         
     }
     
     public void editAula(String nom, int capacitat, TipusAula tipusAula, int id) {
         Aula aula = new Aula(id, nom, capacitat, tipusAula);
         aules.remove(id);
         aules.add(id, aula);
         saveState();
     }
     
     public int getNumAules() {
         //NOTA: Si s'eliminen aules, al iterar el vector tipusAules faria saltar una excepció ja que intentaria accedir a una posició del vector inexistent
         return aules.size();
     }
     
    public Aula getAula(int idAula) {
         int numAules = getNumAules();
         for (int i = 0; i < numAules; ++i) {
             Aula aula = aules.get(i);
             if (aula.getID() == idAula) {
                 return aula;
             }
         }
         return null; 
     }
    
     public Aula getAula(String nom) {
         int numAules = getNumAules();
         for (int i = 0; i < numAules; ++i) {
             Aula aula = aules.get(i);
             if (aula.getNom().equals(nom)) {
                 return aula;
             }
         }
         return null; 
     }
     
    public Aula getAulaAt(int index) {
         return aules.get(index);
     } 
    
    public int getIndexOfAula(Aula aula) {
         return aules.indexOf(aula);
     }
    
    public void removeAula(Aula aula) {
         aules.removeElement(aula);
         saveState();
     }
       
     /** Retorna cert si existeix l'objecte grau al vector graus. **/
     public boolean hasGrau(Grau grau) {
         return graus.contains(grau);
     }
     
     /** Retorna cert si existeix l'objecte TipusAula al vector tipusAules. **/
     public boolean hasTipusAula(TipusAula tipusAula) {
         return tipusAules.contains(tipusAula);
     }
     
     /** Retorna cert si existeix l'objecte TipusMateria al vector tipusMateries. **/
     public boolean hasTipusMateria(TipusMateria tipus) {
         return tipusMateries.contains(tipus);
     }
     
     /** Retorna cert si existeix l'objecte Aula al vector aules. **/
     public boolean hasAula(Aula aula) {
         return aules.contains(aula);
     }
     
     public int getIndexOfGrau(Grau grau) {
         return graus.indexOf(grau);
     }
   
     
     /** Retorna cert si existeix l'objecte TipusHoresPractica al vector tipusHoresPractiques. **/
     /*public boolean hasTipusHoresPractica(TipusHoresPractica tipus) {
         return tipusHoresPractiques.contains(tipus);
     }*/
     
     
     /** Actualitza la llista de dates "diesDocencia" amb les dades del fitxer "selectedDates.ser". */
     public void loadDiesDocencia() {
         FileInputStream fin;
         ObjectInputStream ois;
         try {
             fin = new FileInputStream("selectedDates.ser");
             try {
                 ois = new ObjectInputStream(fin);
                 try {
                     diesDocencia = (List<Date>) ois.readObject();
                 } catch (ClassNotFoundException ex) {
                     Logger.getLogger(SelectorDiesDocencia.class.getName()).log(Level.SEVERE, null, ex);
                 }
             } catch (IOException ex) {
                 Logger.getLogger(SelectorDiesDocencia.class.getName()).log(Level.SEVERE, null, ex);
             }
             
         } catch (FileNotFoundException ex) {
             Logger.getLogger(SelectorDiesDocencia.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     /** Retorna una List<Date> que conté tots els dies marcats com a docents donat un quadrimestre (1 o 2) i l'any. **/
     public List<Date> getDiesDocencia(int quadri, int any) {
         Date q1_inici = new GregorianCalendar(any, 7, 1).getTime();
         Date q1_final = new GregorianCalendar(any + 1, 1, 1).getTime();
         Date q2_inici = new GregorianCalendar(any, 1, 2).getTime();
         Date q2_final = new GregorianCalendar(any, 6, 25).getTime();
         
         Calendar temp = new GregorianCalendar();
         if (quadri == 1) setmanaOrdreQ1.clear();
         if (quadri == 2) setmanaOrdreQ2.clear();
         
         List<Date> dies = new ArrayList<Date>();
         Iterator<Date> it = diesDocencia.listIterator();
         while (it.hasNext()) {
             //System.out.println(iterator.next());
             Date d = it.next();
             temp.setTime(d);
             int weekOfYear = temp.get(Calendar.WEEK_OF_YEAR);
             if (weekOfYear <= 5) weekOfYear +=53;
             if (quadri == 1) {
                 if (d.after(q1_inici) && d.before(q1_final)) {
                     dies.add(d);
                     if (setmanaOrdreQ1.indexOf(weekOfYear) == -1)
                         setmanaOrdreQ1.addElement(new Integer(weekOfYear));
                 }
             } else if (quadri == 2) {
                 if (d.after(q2_inici) && d.before(q2_final)) {
                     dies.add(d);
                     if (setmanaOrdreQ2.indexOf(weekOfYear) == -1)
                         setmanaOrdreQ2.addElement(new Integer(weekOfYear));
                 }
             }
         }
         if (quadri == 1) Collections.sort(setmanaOrdreQ1);
         if (quadri == 2) {
             Collections.sort(setmanaOrdreQ2);
             for (int i = 0; i < setmanaOrdreQ2.size(); ++i) {
                 System.out.println(Integer.toString(i).concat(" = ").concat(Integer.toString((Integer)setmanaOrdreQ2.get(i))));
             }
         }
         //Collections.copy(dies, diesDocencia);
         
         return dies;
     }
     
     /** retorna un valor del 0 al 3, corresponents a setmana 11, 21, 12 i 22 donada una data i un quadrimestre (1 o 2). Retorna -1 si quadrimestre != 1 o 2. **/
     public int getSetmanaOrdre(Date date, int quadri) {
         if (quadri == 1 && setmanaOrdreQ1.isEmpty()) getDiesDocencia(quadri, 2012);
         if (quadri == 2 && setmanaOrdreQ2.isEmpty()) getDiesDocencia(quadri, 2013);
         Calendar dia = new GregorianCalendar();
         dia.setTime(date);
         int weekOfYear = dia.get(Calendar.WEEK_OF_YEAR);
         if (weekOfYear <= 5) weekOfYear +=53;
         if (quadri == 1) {
             return setmanaOrdreQ1.indexOf(weekOfYear) % 4;
         } else if (quadri == 2) {
             return setmanaOrdreQ2.indexOf(weekOfYear) % 4;
         }
         
         return -1;
     }
     
     public void updateDisponibilitatHoraria() {
         int any = Regles.CONVOCATORIA.getInt() + 2010 + Regles.QUADRIMESTRE.getInt();
         updateDisponibilitatHoraria(Regles.QUADRIMESTRE.getInt(), any);
         
     }
     
     /** Retorna les assignatures corresponents a un grau i un curs donat.
      * 
      * @param grau objecte tipus Grau
      * @param curs enter indicant el curs on s'imparteix l'assignatura (1..8) (Atribut quadrimestre de la classe Assignatura)
      */
     public Vector<Assignatura> getAssignatures(Grau grau, int curs) {
         dbg("getAssignatures(".concat(grau.getNom()).concat(", ").concat(String.valueOf(curs)).concat(")"));
         int numAssignatures = getNumAssignatures();
         Vector<Assignatura> seleccio = new Vector<Assignatura>();
         Assignatura assig;
         for (int i = 0; i < numAssignatures; ++i) {
             assig = getAssignaturaAt(i);
             dbg(assig.getGrau().getNom().concat(" == ").concat(grau.getNom()).concat(" && ").concat(String.valueOf(assig.getQuadrimestre()).concat(" == ").concat(String.valueOf(curs))));
             if (assig.getGrau().equals(grau) && assig.getQuadrimestre() == curs) {
                 seleccio.add(assig);
                 dbg("ADD ".concat(assig.getNom()));
             }
         }
         return seleccio;
     }
     
     /** Genera la disponibilitatHoraria inicial donat el quadrimestre i l'any
      * 
      * @param quadri enter (1 o 2)
      * @param any enter. ex: 2012
      */
     public void updateDisponibilitatHoraria(int quadri, int any) {
         List<Date> dies = getDiesDocencia(quadri, any);
         disponibilitatsHoraries = new Vector<DisponibilitatHoraria>();
         DisponibilitatHoraria disponibilitat = new DisponibilitatHoraria();
         Calendar dia = new GregorianCalendar();
         int ordre = 0, mod;
         int diaDeLaSetmana = 0;
 
         Iterator<Date> it = dies.listIterator();
         while (it.hasNext()) {
             //System.out.println(iterator.next());
             Date d = it.next();
             dia.setTime(d);
             //if ((mod = ModsCalendari.getModOrdre(dia)) != -1)
             if (Regles.APLICAR_MODS_CALENDARI.get()) {
                 ordre = ((mod = ModsCalendari.getModOrdre(dia)) != -1) ? mod : getSetmanaOrdre(d, quadri);
                 diaDeLaSetmana = ((mod = ModsCalendari.getModDia(dia)) != -1) ? mod : dia.get(Calendar.DAY_OF_WEEK);
             } else {
                 //No apliquem classe ModsCalendari
                 ordre = getSetmanaOrdre(d, quadri);
                 diaDeLaSetmana = dia.get(Calendar.DAY_OF_WEEK);
             }
             disponibilitat.addDia(diaDeLaSetmana, ordre);
             
         }
         disponibilitatsHoraries.add(disponibilitat);
         //int horesDesquadrades = 0;
         //try {
             
             //addAssignatura(new Grau("Informatica", "01"), "FISI", 205555, new TipusMateria(1, "ABC"),
             //        25, 2, 20, 40, new TipusHoresPractica(5, "Laboratori Informàtica"), 1);
             /*//PROP
             horesDesquadrades = disponibilitat.addReserva(30, "340380#0#0");    //TEORIA CONJUNTA
             System.out.println("RESERVA 30h [340380#0#0]: ".concat(String.valueOf(horesDesquadrades)));
             disponibilitat.imprimeixPonderacio();
             horesDesquadrades = disponibilitat.addReserva(30, "340380#5#1");    //LAB GRUP 1
             System.out.println("RESERVA 30h [340380#5#1]: ".concat(String.valueOf(horesDesquadrades)));
             disponibilitat.imprimeixPonderacio();
             horesDesquadrades = disponibilitat.addReserva(30, "340380#5#2");    //LAB GRUP 2
             System.out.println("RESERVA 30h [340380#5#2]: ".concat(String.valueOf(horesDesquadrades)));
             disponibilitat.imprimeixPonderacio();
             //XACO
             horesDesquadrades = disponibilitat.addReserva(42, "340356#0#0");    //TEORIA CONJUNTA
             System.out.println("RESERVA 42h [340356#0#0]: ".concat(String.valueOf(horesDesquadrades)));
             disponibilitat.imprimeixPonderacio();
             horesDesquadrades = disponibilitat.addReserva(18, "340356#5#1");    //LAB GRUP 1
             System.out.println("RESERVA 18h [340356#5#1]: ".concat(String.valueOf(horesDesquadrades)));
             disponibilitat.imprimeixPonderacio();
             horesDesquadrades = disponibilitat.addReserva(18, "340356#5#2");    //LAB GRUP 2
             System.out.println("RESERVA 18h [340356#5#2]: ".concat(String.valueOf(horesDesquadrades)));
             disponibilitat.imprimeixPonderacio();
             horesDesquadrades = disponibilitat.addReserva(18, "340356#5#3");    //LAB GRUP 3
             System.out.println("RESERVA 18h [340356#5#3]: ".concat(String.valueOf(horesDesquadrades)));
             disponibilitat.imprimeixPonderacio();
             //CLON DE XACO
             horesDesquadrades = disponibilitat.addReserva(42, "370251#0#0");    //TEORIA CONJUNTA
             System.out.println("RESERVA 42h [370251#0#0]: ".concat(String.valueOf(horesDesquadrades)));
             disponibilitat.imprimeixPonderacio();
             horesDesquadrades = disponibilitat.addReserva(18, "370251#5#1");    //LAB GRUP 1
             System.out.println("RESERVA 18h [370251#5#1]: ".concat(String.valueOf(horesDesquadrades)));
             disponibilitat.imprimeixPonderacio();
             horesDesquadrades = disponibilitat.addReserva(18, "370251#5#2");    //LAB GRUP 2
             System.out.println("RESERVA 18h [370251#5#2]: ".concat(String.valueOf(horesDesquadrades)));
             disponibilitat.imprimeixPonderacio();
             //CLON DE PROP 1
             horesDesquadrades = disponibilitat.addReserva(30, "390123#0#0");    //TEORIA CONJUNTA
             System.out.println("RESERVA 30h [390123#0#0]: ".concat(String.valueOf(horesDesquadrades)));
             disponibilitat.imprimeixPonderacio();
             horesDesquadrades = disponibilitat.addReserva(30, "390123#5#1");    //LAB GRUP 1
             System.out.println("RESERVA 30h [390123#5#1]: ".concat(String.valueOf(horesDesquadrades)));
             disponibilitat.imprimeixPonderacio();
             Regles.DEBUG2_ENABLED.set(true);
             horesDesquadrades = disponibilitat.addReserva(30, "390123#5#2");    //LAB GRUP 2
             System.out.println("RESERVA 30h [390123#5#2]: ".concat(String.valueOf(horesDesquadrades)));
             disponibilitat.imprimeixPonderacio();
             Regles.DEBUG2_ENABLED.set(false);
             //CLON DE PROP 2
             horesDesquadrades = disponibilitat.addReserva(30, "300000#0#0");    //TEORIA CONJUNTA
             System.out.println("RESERVA 30h [300000#0#0]: ".concat(String.valueOf(horesDesquadrades)));
             disponibilitat.imprimeixPonderacio();
             horesDesquadrades = disponibilitat.addReserva(30, "300000#5#1");    //LAB GRUP 1
             System.out.println("RESERVA 30h [300000#5#1]: ".concat(String.valueOf(horesDesquadrades)));
             disponibilitat.imprimeixPonderacio();
             horesDesquadrades = disponibilitat.addReserva(30, "300000#5#2");    //LAB GRUP 2
             System.out.println("RESERVA 30h [300000#5#2]: ".concat(String.valueOf(horesDesquadrades)));
             disponibilitat.imprimeixPonderacio();
             
             
 
         } catch (CloneNotSupportedException ex) {
             Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
         }
         * */
         
         progres = 0;
         /*WaitDialog dialog = new WaitDialog(new javax.swing.JFrame(), true);
         dialog.addWindowListener(new java.awt.event.WindowAdapter() {
 
         });
         SwingWorker worker = new SwingWorker() {
 
             @Override
             protected Object doInBackground() throws Exception {
                 register();
                 return 0;
             }
 
             @Override
             protected void done() {
                 dialog.dispose();
             }
         };
         worker.execute();
         dialog.setVisible(true);^*/
         //SwingUtilities.invokeAndWait(null);
 
         
         
     }
     
     public Vector<DisponibilitatHoraria> getDisponibilitatsHoraries() {
         return disponibilitatsHoraries;
     }
     
     /** Afegeix el missatge com a una nova línea de l'informe de resultats. **/
     public void informar(String msg) {
         String newLine = "<br>";
         informe = informe.concat(msg).concat(newLine);
     }
     
     public void saveState() {
         FileOutputStream fout;
         //FileOutputStream fout2;
         ObjectOutputStream oos;
         try {
             fout = new FileOutputStream("graus.ser");
             oos = new ObjectOutputStream(fout);
             oos.writeObject(graus);
             fout = new FileOutputStream("tipusAules.ser");
             oos = new ObjectOutputStream(fout);
             oos.writeObject(tipusAules);
             fout = new FileOutputStream("aules.ser");
             oos = new ObjectOutputStream(fout);
             oos.writeObject(aules);
             fout = new FileOutputStream("tipusMateries.ser");
             oos = new ObjectOutputStream(fout);
             oos.writeObject(tipusMateries);
             /*fout = new FileOutputStream("tipusHoresPractiques.ser");
             oos = new ObjectOutputStream(fout);
             oos.writeObject(tipusHoresPractiques);*/
             fout = new FileOutputStream("assignatures.ser");
             oos = new ObjectOutputStream(fout);
             oos.writeObject(assignatures);
 
             
         } catch (FileNotFoundException ex) {
             Logger.getLogger(SelectorDiesDocencia.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(SelectorDiesDocencia.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     public void loadState(String directori) {
         //Si directory no és buit, eliminem les dades de l'última sessió i en copiem les del directori especificat
         if (!directori.isEmpty()) {
             //eliminem les dades de l'última sessió
             (new File("graus.ser")).delete();
             (new File("selectedDates.ser")).delete();
             (new File("tipusAules.ser")).delete();
             (new File("aules.ser")).delete();
             (new File("tipusMateries.ser")).delete();
             //(new File("tipusHoresPractiques.ser")).delete();
             (new File("assignatures.ser")).delete();
             //copiem el contingut del directori de dades a carregar
             try {
                 HourCalendar.copyDirectory(new File(directori), new File("."));
             } catch (IOException ex) {
                 Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         FileInputStream fin;
         ObjectInputStream ois;
         try {
             if (new File("selectedDates.ser").exists()) {
                 fin = new FileInputStream("selectedDates.ser");
                 ois = new ObjectInputStream(fin);
                 diesDocencia = (List<Date>) ois.readObject();
             }
             if (new File("graus.ser").exists()) {
                 fin = new FileInputStream("graus.ser");
                 ois = new ObjectInputStream(fin);
                 graus = (Vector<Grau>) ois.readObject();
             }
             if (new File("tipusAules.ser").exists()) {
                 fin = new FileInputStream("tipusAules.ser");
                 ois = new ObjectInputStream(fin);
                 tipusAules = (Vector<TipusAula>) ois.readObject();
             }
             if (new File("aules.ser").exists()) {
                 fin = new FileInputStream("aules.ser");
                 ois = new ObjectInputStream(fin);
                 aules = (Vector<Aula>) ois.readObject();
             }
             if (new File("tipusMateries.ser").exists()) {
                 fin = new FileInputStream("tipusMateries.ser");
                 ois = new ObjectInputStream(fin);
                 tipusMateries = (Vector<TipusMateria>) ois.readObject();
             }
             /*if (new File("tipusHoresPractiques.ser").exists()) {
                 fin = new FileInputStream("tipusHoresPractiques.ser");
                 ois = new ObjectInputStream(fin);
                 tipusHoresPractiques = (Vector<TipusHoresPractica>) ois.readObject();
             }*/
             if (new File("assignatures.ser").exists()) {
                 fin = new FileInputStream("assignatures.ser");
                 ois = new ObjectInputStream(fin);
                 assignatures = (Vector<Assignatura>) ois.readObject();
             }
 
         } catch (FileNotFoundException ex) {
             Logger.getLogger(SelectorDiesDocencia.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(SelectorDiesDocencia.class.getName()).log(Level.SEVERE, null, ex);
         } catch (ClassNotFoundException ex) {
             Logger.getLogger(SelectorDiesDocencia.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     public static class ModsCalendari {
         public static Vector< Vector<Calendar> > datesAmbDiaModificat;
         public static Vector< Vector<Calendar> > datesAmbOrdreModificat;
 
         public static void load() {
             datesAmbDiaModificat = new Vector< Vector<Calendar> >();
             for (int i = 0; i <= 7; ++i)
                 datesAmbDiaModificat.add(new Vector<Calendar>());
             //MODS Q2 2012-2013
             //Marquem el dijous 25 d'abril de 2013 com a dimarts
             datesAmbDiaModificat.get(3).add(new GregorianCalendar(2013, 3, 25));
             datesAmbDiaModificat.get(4).add(new GregorianCalendar(2013, 4, 3)); //3 de maig com dimecres
 
             datesAmbOrdreModificat = new Vector< Vector<Calendar> >();
             for (int i = 0; i <= 3; ++i)
                 datesAmbOrdreModificat.add(new Vector<Calendar>());
 
             datesAmbOrdreModificat.get(3).add(new GregorianCalendar(2013, 5, 7));
             datesAmbOrdreModificat.get(0).add(new GregorianCalendar(2013, 5, 10));
             //MODS Q1 2012-2013
             datesAmbOrdreModificat.get(3).add(new GregorianCalendar(2012, 10, 29)); //29 nov 2012 com setmana 2 2
             datesAmbDiaModificat.get(5).add(new GregorianCalendar(2013, 0, 14));    //14 gener 2013 com dijous
             datesAmbDiaModificat.get(6).add(new GregorianCalendar(2013, 0, 15));    //15 gener 2013 com divendres
             
         }
         
         /** Retorna el dia de la setmana (1 = diumenge) d'un dia específic, -1 si no és un dia modificat. **/
         public static int getModDia(Calendar dia) {
             for (int i = 0; i <= 7; ++i) {
                 if (datesAmbDiaModificat.get(i).indexOf(dia) != -1) {
                     dbg("MOD DIA!!!");
                     return i;
                 }
             }
             return -1;
         }
         
         /** Retorna l'ordre d'un dia específic, -1 si no és un dia amb ordre modificat. 
          *
          * @return Integer entre 0 i 3 corresponents a setmana 11, 21, 12 i 22 respectivament.
          */
         public static int getModOrdre(Calendar dia) {
             for (int i = 0; i <= 3; ++i) {
                 if (datesAmbOrdreModificat.get(i).indexOf(dia) != -1) {
                     dbg("MOD ORDRE!!!");
                     return i;
                 }
             }
             return -1;
         }
     }
     
     /*public static class Regles {
         public static boolean solaparHoresPractica = true;
         public static String solaparHoresPracticaMissatge = "Solapar les hores de pràctica d'assignatures i grups diferents.";
         
     }*/
     
    public enum Regles {
         SOLAPAR_HORES_PRACTICA (1, "<html>Solapar les hores de pràctica d'assignatures i grups diferents.</html>"),
         ASSIGNAR_HORES_RESTANTS (0, "<html>Sobrepassar les hores assignades a les assignatures si no <br>existeix combinació possible que les quadri al calendari.</html>"),
         PRIORITZAR_QUADRAR_HORES (1, "<html>Donar prioritat a quadrar les hores de les assignatures enlloc <br>d'obtenir un calendari més ben repartit.</html>"),
         ITERACIONS_GENERADOR (200, "<html>Número d'iteracions que realitza el generador <br>d'horaris per a trobar la millor combinació.</html>"),
         QUADRIMESTRE (2, "<html>Quadrimestre per al que generar els horaris.</html>"),
         CONVOCATORIA (1, "<html>Any de convocatòria.</html>"),
         APLICAR_MODS_CALENDARI (1, "<html>Utilitzar modificacions del calendari. Exemple: Dilluns 14 de gener passa a ser dijous.</html>"),
         //REGLES INTERNES (NO MODIFICABLES A TRAVÉS DEL FORMULARI D'OPCIONS
         DEBUG_ENABLED (0, "Mostrar informació per consola de les operacions que es van realitzant (DEBUG MODE)."),
         DEBUG2_ENABLED (0, "Mostrar informació per consola d'altres operacions que es van realitzant (DEBUG2 MODE)."),
         DEBUG_AUX_ENABLED (1, "Mostrar informació per consola d'operacions auxiliars (DEBUG AUX MODE)."),
         DEBUG_UI_ENABLED (0, "Mostrar informació per consola d'operacions gràfiques que es van realitzant (DEBUG UI MODE)."),
         UTILITZA_ANTIC_ALGORISME (0, "Utilitzar l'antic algorisme per a generar els horaris. No soporta solapament d'hores de pràctica.");
 
         private int valor;
         private String missatge;
         
         Regles(int valor, String missatge) {
             this.valor = valor;
             this.missatge = missatge;
         }
         public boolean get() { return (valor == 0) ? false : true; }
         public int getInt() { return valor; }
         public void set(int valor) { this.valor = valor; }
         public void set(boolean valor) { this.valor = (valor) ? 1 : 0; }
         public String getMissatge() { return missatge; }
         
     }
     
     public static void dbg(String msg) {
         if (Regles.DEBUG_ENABLED.get()) {
             System.out.println(msg);
         }
     }
     
     public static void dbg2(String msg) {
         if (Regles.DEBUG2_ENABLED.get()) {
             System.out.println(msg);
         }
     }
     
     public static void dbgAUX(String msg) {
         if (Regles.DEBUG_AUX_ENABLED.get()) {
             System.out.println("[AUX] ".concat(msg));
         }
     }
     
     public static void dbgUI(String msg) {
         if (Regles.DEBUG_UI_ENABLED.get()) {
             System.out.println("[dbgUI] ".concat(msg));
         }
     }
 }
