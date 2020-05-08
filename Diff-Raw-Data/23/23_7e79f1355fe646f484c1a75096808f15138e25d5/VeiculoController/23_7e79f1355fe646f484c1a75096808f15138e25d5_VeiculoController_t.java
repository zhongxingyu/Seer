 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.controller;
 
 import com.model.dao.OpcionaisVeiculoDao;
 import com.model.dao.VeiculoDao;
 import com.model.entity.OpcionaisVeiculo;
 import com.model.entity.TipoVeiculo;
 import com.model.entity.Veiculo;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.RequestScoped;
 import javax.faces.context.FacesContext;
 import javax.servlet.http.HttpServletRequest;
 
 /**
  *
  * @author renanmarceluchoa
  */
 @ManagedBean
 @RequestScoped
 public class VeiculoController implements Serializable {
     
     @EJB
     private VeiculoDao dao;
     @EJB
     private OpcionaisVeiculoDao opcionaisDao;
     
     private Veiculo veiculo;
     private List<Veiculo> veiculos;
     
     @ManagedProperty(name="id", value="#{param.id}")
     private Integer id;
     
     private TipoVeiculo tipoSelecionado;
     private Map<Integer,Boolean> checked;
     
     public VeiculoController() {
         this.veiculo = new Veiculo();
     }
     
     public String salvar() {
         veiculo.setTipoVeiculo(tipoSelecionado);
         relacionarOpcionais();
         if(veiculo.getId() == null) {
             dao.inserir(veiculo);
             this.veiculos = dao.listar();
         } else {
             dao.atualizar(veiculo);
             this.veiculos = dao.listar();
         }
         return "listaVeiculo";
     }
     
     public String editar() {
        for (Veiculo veiculo : veiculos) {
            if (veiculo.getId() == id) {
                System.out.println("v"+veiculo.getId()+": " + veiculo);
                this.veiculo = veiculo;
            }
        }
         System.out.println("Veiculo:"+id);
         System.out.println("Veiculo:"+veiculo.getId());
         return "formulario";
     }
     
     public String deletar() {
         veiculo = dao.buscar(id);
         dao.remover(veiculo);
         System.out.println("Veiculo:"+id);
         System.out.println("Veiculo:"+veiculo.getId());
         this.veiculos = dao.listar();
         return "listaVeiculo";
     }
     
     @PostConstruct
     public void listar() {
         this.veiculo = new Veiculo();
         this.veiculo.setOpcionaisVeiculo(new ArrayList<OpcionaisVeiculo>());
         this.veiculos = dao.listar();
         if (checked == null || checked.isEmpty()) {
             this.checked = this.getCheckedList();
         }
     }
     
     public Map<Integer,Boolean> getCheckedList() {
         Map<Integer,Boolean> checkedList = new HashMap<Integer,Boolean>();
         List<OpcionaisVeiculo> opcionais = opcionaisDao.listar();
         for (OpcionaisVeiculo opcional : opcionais) {
             boolean flag = false;
             if (veiculo.getOpcionaisVeiculo() != null) {
                 for (OpcionaisVeiculo opcionalVeiculo : veiculo.getOpcionaisVeiculo()) {
                     if(opcional.getId() == opcionalVeiculo.getId()) {
                         checkedList.put(opcional.getId(), Boolean.TRUE);
                         flag = true;
                     }
                 }
             }
             if(flag == false) {
                 checkedList.put(opcional.getId(), Boolean.FALSE);
             }
         }
         return checkedList;
     }
     
     public void relacionarOpcionais() {
         List<OpcionaisVeiculo> opcionais = opcionaisDao.listar();
         for (OpcionaisVeiculo opcional : opcionais) {
             if (checked.get(opcional.getId())) {
                 veiculo.getOpcionaisVeiculo().add(opcional);
             }
         }
     }
     
     public Integer getParam() {
         FacesContext context = FacesContext.getCurrentInstance();
         HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
         return Integer.getInteger(request.getParameter("id"));
     }
 
     /**
      * @return the veiculo
      */
     public Veiculo getVeiculo() {
         return veiculo;
     }
 
     /**
      * @param veiculo the veiculo to set
      */
     public void setVeiculo(Veiculo veiculo) {
         this.veiculo = veiculo;
     }
 
     /**
      * @return the veiculos
      */
     public List<Veiculo> getVeiculos() {
         return veiculos;
     }
 
     /**
      * @param veiculos the veiculos to set
      */
     public void setVeiculos(List<Veiculo> veiculos) {
         this.veiculos = veiculos;
     }
 
     /**
      * @return the tipoSelecionado
      */
     public TipoVeiculo getTipoSelecionado() {
         return tipoSelecionado;
     }
 
     /**
      * @return the veiculoSelected
      */
     public Integer getId() {
         return id;
     }
 
     /**
      * @param veiculoSelected the veiculoSelected to set
      */
     public void setId(Integer id) {
         this.id = id;
     }
 
     /**
      * @param tipoSelecionado the tipoSelecionado to set
      */
     public void setTipoSelecionado(TipoVeiculo tipoSelecionado) {
         this.tipoSelecionado = tipoSelecionado;
     }
 
     /**
      * @return the checked
      */
     public Map<Integer,Boolean> getChecked() {
         return checked;
     }
 
     /**
      * @param checked the checked to set
      */
     public void setChecked(Map<Integer,Boolean> checked) {
         this.checked = checked;
     }
     
 }
