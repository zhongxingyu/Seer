 package com.rochamarinho.controller;
 
 import com.rochamarinho.backend.FilialBackend;
 import com.rochamarinho.backend.impl.MySQLFilialBackend;
 import com.rochamarinho.model.Advogado;
 import com.rochamarinho.model.Filial;
 import com.rochamarinho.utils.BackendException;
 import java.util.List;
 
 /**
  *
  * @author nicolas
  */
 public class FilialController {
 
     MySQLFilialBackend backend;
     
     public FilialController() {
     }
     
     public FilialBackend getBackend() {
         if (backend == null)
         {
             backend = new MySQLFilialBackend();
         }
         return backend;
     }
 
     public void setBackend(MySQLFilialBackend backend) {
         this.backend = backend;
     }
     
    public boolean cadastrarFilial(String nome) throws BackendException
     {
        if (getBackend().buscarPorNome(nome) != null)
        {
            return false;
        }               
         Filial filial = new Filial();
         filial.setNome(nome);
         
         getBackend().create(filial);
        return true;
     }
     
     public List<Filial> listarFiliais() throws BackendException
     {
         return getBackend().list();
     }
     
     public void addAdvogadoNumaFilial(Filial filial,Advogado adv)
     {
         
     }
     
     public Filial porNome(String text) throws BackendException {
         
         return getBackend().buscarPorNome(text);
         
     }
 
     public void atualizarFilial(Filial antiga, String novaFilial) throws BackendException {
         
         Filial f = getBackend().read(antiga.getId());
         f.setNome(novaFilial);
         getBackend().update(f);
         
     }
 
 
     
     
     
 }
