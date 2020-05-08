 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package loadkdd;
 
 import ControladoresJPA.FabricaObjetos;
 import ControladoresJPA.MedicamentoJpaController;
 import ControladoresJPABodega.FabricaObjetosBodega;
 import ControladoresJPABodega.MedicamentoBodegaJpaController;
 import Entidades_OLTP.Medicamento;
 import Entidades_bodega.MedicamentoBodega;
 import java.util.List;
 import javax.persistence.EntityManager;
 
 /**
  *
  * @author USER
  */
 public class CargaMedicamento {
 
     private FabricaObjetos mi_fabrica;
     private FabricaObjetosBodega fabrica_bodega;
     ControladoresJPA.MedicamentoJpaController controladorBD;
     ControladoresJPABodega.MedicamentoBodegaJpaController controladorBodega;
     EntityManager manager;
     EntityManager manager_bodega;
 
     public CargaMedicamento() {
         mi_fabrica = new FabricaObjetos();
         fabrica_bodega=new FabricaObjetosBodega();
         manager = mi_fabrica.crear().createEntityManager();
         manager_bodega=fabrica_bodega.crear().createEntityManager();
         controladorBD=new MedicamentoJpaController(mi_fabrica.getFactory());
         controladorBodega=new MedicamentoBodegaJpaController(fabrica_bodega.getFactory());
     }
 
     public void carga() {
         List lista;
        // lista = manager.createQuery("SELECT * FROM medicamentos m").getResultList();
         lista=controladorBD.findMedicamentoEntities();
         for (int i = 0; i < lista.size(); i++) {
 //            System.out.println("-------AQUII----"+i);
             Medicamento m = (Medicamento) lista.get(i);
              System.out.println(m.getCodigo());
             //crear nuevo objeto
              
            MedicamentoBodega medicamentoNuevo = new MedicamentoBodega();
             //para añadir campos al medicamento
             
             medicamentoNuevo.setCodigoMedicamento(m.getCodigo().toString());
             medicamentoNuevo.setNombre(m.getNombreGenerico().toString());
             medicamentoNuevo.setFormaFarmaceutica(m.getFormaFarmaceutica().toString());
             medicamentoNuevo.setPresentacion(m.getPresentacion().toString());
             medicamentoNuevo.setLabRegistro(m.getLaboratorioRegistro());
             medicamentoNuevo.setPrecio(m.getPrecio());
             medicamentoNuevo.setTipo(m.getTipoMedicamento().toString());
 
            controladorBodega.create(medicamentoNuevo);
 
         }
 
     }
 }
