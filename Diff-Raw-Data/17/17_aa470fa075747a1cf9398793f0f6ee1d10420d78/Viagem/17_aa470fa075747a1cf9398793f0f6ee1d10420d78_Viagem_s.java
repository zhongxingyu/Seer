 package com.model.entity;
 
 import com.model.dao.PassageiroDAO;
 import com.model.dao.SolicitacaoViagemDAO;
 import com.model.dao.UsuarioDAO;
 import com.model.dao.VeiculoDAO;
 import com.model.dao.ViagemDAO;
 import java.sql.Connection;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 /**
  *
  * @author Usuario
  */
 public class Viagem implements Entity {
 
     private Integer idViagem;
     private Calendar dataEfetivacao;
     //private Usuario autorizante;
     private String autorizante;
     private Usuario motorista;
     private Veiculo veiculo;
     private Calendar dataSaida;
     private Calendar dataRetorno;
     private Calendar horaSaida;
     private Calendar horaRetorno;
     private String objetivo;
     private String percurso;
     private String localSaida;
     private String localRetorno;
     private List<Passageiro> passageiros;
     private List<SolicitacaoViagem> solicitacoes;
 
     /**
      * @return the idViagem
      */
     public Integer getIdViagem() {
         return idViagem;
     }
 
     /**
      * @param idViagem the idViagem to set
      */
     public void setIdViagem(Integer idViagem) {
         this.idViagem = idViagem;
     }
 
     /**
      * @return the dataEfetivacao
      */
     public Date getDataEfetivacao() {
         if (dataEfetivacao != null) {
             return dataEfetivacao.getTime();
         }
         return null;
     }
 
     /**
      * @return the dataEfetivacao
      */
     public String getDataEfetivacaoFormatada() {
         if (dataEfetivacao != null) {
             return new SimpleDateFormat("dd/MM/yyyy").format(dataEfetivacao.getTime());
         }
         return null;
     }
 
     /**
      * @param dataEfetivacao the dataEfetivacao to set
      */
     public void setDataEfetivacao(Date dataEfetivacao) {
         if (dataEfetivacao != null) {
             Calendar cal = new GregorianCalendar();
             cal.setTime(dataEfetivacao);
             this.dataEfetivacao = new GregorianCalendar(cal.get(GregorianCalendar.YEAR), cal.get(GregorianCalendar.MONTH), cal.get(GregorianCalendar.DAY_OF_MONTH));
         } else {
             this.dataEfetivacao = null;
         }
     }
 
 //    /**
 //     * @return the autorizante
 //     */
 //    public Usuario getAutorizante() {
 //        return autorizante;
 //    }
 //
 //    /**
 //     * @param autorizante the autorizante to set
 //     */
 //    public void setAutorizante(Usuario autorizante) {
 //        this.autorizante = autorizante;
 //    }
     /**
      * @return the autorizante
      */
     public String getAutorizante() {
         return autorizante;
     }
 
     /**
      * @param autorizante the autorizante to set
      */
     public void setAutorizante(String autorizante) {
         this.autorizante = autorizante;
     }
 
     /**
      * @return the motorista
      */
     public Usuario getMotorista(Connection connection) {
         
         if (motorista != null) {
             if (motorista.getNome() == null) {
                 UsuarioDAO udao = new UsuarioDAO(connection);
                 motorista = udao.getById(motorista.getIdUsuario());
             }
         }
         return motorista;
     }
     
     /**
      * @return the motorista
      */
     public Usuario getMotorista() {
         return motorista;
     }
 
     /**
      * @param motorista the motorista to set
      */
     public void setMotorista(Usuario motorista) {
         this.motorista = motorista;
     }
 
     /**
      * @return the veiculo
      */
     public Veiculo getVeiculo(Connection connection) {
         if (veiculo != null) {
             if (veiculo.getPlaca() == null) {
                 VeiculoDAO vdao = new VeiculoDAO(connection);
                 veiculo = (Veiculo) vdao.getById(veiculo.getIdVeiculo());
             }
         }
         return veiculo;
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
      * @return the passageiros
      */
     public List<Passageiro> getPassageiros(Connection connection) {
         if (this.passageiros == null) {
             this.passageiros = new ArrayList<Passageiro>();
             PassageiroDAO pdao = new PassageiroDAO(connection);
             for (Integer id : new ViagemDAO(connection).getPassIds(this.idViagem)) {
                 this.passageiros.add(pdao.getById(id));
             }
         }
         return passageiros;
     }
 
     /**
      * @param passageiros the passageiros to set
      */
     public void setPassageiros(List<Passageiro> passageiros) {
         this.passageiros = passageiros;
     }
 
     /**
      * @param passageiro the passageiro to add
      */
     public boolean addPassageiro(Passageiro passageiro) {
         if (this.passageiros == null) {
             this.passageiros = new ArrayList<Passageiro>();
         }
         return this.passageiros.add(passageiro);
     }
 
     /**
      * @return the solicitacoes
      */
     public List<SolicitacaoViagem> getSolicitacoes(Connection connection) {
         if (solicitacoes == null) {
             solicitacoes = new ArrayList<SolicitacaoViagem>();
             SolicitacaoViagemDAO sdao = new SolicitacaoViagemDAO(connection);
             for (Integer sv : new ViagemDAO(connection).getSolIds(this.idViagem)) {
                 solicitacoes.add(sdao.getById(sv));
             }
         }
         return solicitacoes;
     }
 
     /**
      * @param solicitacoes the solicitacoes to set
      */
     public void setSolicitacoes(List<SolicitacaoViagem> solicitacoes) {
         this.solicitacoes = solicitacoes;
     }
 
     /**
      * @param solicitacao the solicitacao to add
      */
     public boolean addSolicitacao(SolicitacaoViagem solicitacao) {
         if (this.solicitacoes == null) {
             this.solicitacoes = new ArrayList<SolicitacaoViagem>();
         }
         return this.solicitacoes.add(solicitacao);
     }
 
     /**
      * @return the dataSaida
      */
     public Date getDataSaida() {
         if (dataSaida != null) {
             return dataSaida.getTime();
         }
         return null;
     }
 
     /**
      * @param dataSaida the dataSaida to set
      */
     public void setDataSaida(Date dataSaida) {
         if (dataSaida != null) {
             Calendar cal = new GregorianCalendar();
             cal.setTime(dataSaida);
             this.dataSaida = new GregorianCalendar(cal.get(GregorianCalendar.YEAR), cal.get(GregorianCalendar.MONTH), cal.get(GregorianCalendar.DAY_OF_MONTH));
         } else {
             this.dataSaida = null;
         }
     }
 
     /**
      * @return the dataSaida
      */
     public String getDataSaidaFormatada() {
         if (dataSaida != null) {
             return new SimpleDateFormat("dd/MM/yyyy").format(dataSaida.getTime());
         }
         return null;
     }
 
     /**
      * @return the dataRetorno
      */
     public Date getDataRetorno() {
         if (dataRetorno != null) {
             return dataRetorno.getTime();
         }
         return null;
     }
 
     /**
      * @param dataRetorno the dataRetorno to set
      */
     public void setDataRetorno(Date dataRetorno) {
         if (dataRetorno != null) {
             Calendar cal = new GregorianCalendar();
             cal.setTime(dataRetorno);
             this.dataRetorno = new GregorianCalendar(cal.get(GregorianCalendar.YEAR), cal.get(GregorianCalendar.MONTH), cal.get(GregorianCalendar.DAY_OF_MONTH));
         } else {
             this.dataRetorno = null;
         }
     }
 
     /**
      * @return the dataRetorno
      */
     public String getDataRetornoFormatada() {
         if (dataRetorno != null) {
             return new SimpleDateFormat("dd/MM/yyyy").format(dataRetorno.getTime());
         }
         return null;
     }
 
     /**
      * @return the horaSaida
      */
     public Date getHoraSaida() {
         if (horaSaida != null) {
             return horaSaida.getTime();
         }
         return null;
     }
 
     /**
      * @param horaSaida the horaSaida to set
      */
     public void setHoraSaida(Date horaSaida) {
         if (horaSaida != null) {
             this.horaSaida = new GregorianCalendar();
             this.horaSaida.setTime(horaSaida);
         } else {
             this.horaSaida = null;
         }
     }
 
     /**
      * @return the horaSaida
      */
     public String getHoraSaidaFormatada() {
         if (horaSaida != null) {
             return new SimpleDateFormat("hh:mm").format(horaSaida.getTime());
         }
         return null;
     }
 
     /**
      * @return the horaRetorno
      */
     public Date getHoraRetorno() {
         if (horaRetorno != null) {
             return horaRetorno.getTime();
         }
         return null;
     }
 
     /**
      * @param horaRetorno the horaRetorno to set
      */
     public void setHoraRetorno(Date horaRetorno) {
         if (horaRetorno != null) {
             this.horaRetorno = new GregorianCalendar();
             this.horaRetorno.setTime(horaRetorno);
         } else {
             this.horaRetorno = null;
         }
     }
 
     /**
      * @return the horaRetorno
      */
     public String getHoraRetornoFormatada() {
         if (horaRetorno != null) {
             return new SimpleDateFormat("hh:mm").format(horaRetorno.getTime());
         }
         return null;
     }
 
     /**
      * @return the objetivo
      */
     public String getObjetivo() {
         return objetivo;
     }
 
     /**
      * @param objetivo the objetivo to set
      */
     public void setObjetivo(String objetivo) {
         this.objetivo = objetivo;
     }
 
     /**
      * @return the percurso
      */
     public String getPercurso() {
         return percurso;
     }
 
     /**
      * @param percurso the percurso to set
      */
     public void setPercurso(String percurso) {
         this.percurso = percurso;
     }
 
     /**
      * @return the localSaida
      */
     public String getLocalSaida() {
         return localSaida;
     }
 
     /**
      * @param localSaida the localSaida to set
      */
     public void setLocalSaida(String localSaida) {
         this.localSaida = localSaida;
     }
 
     /**
      * @return the localRetorno
      */
     public String getLocalRetorno() {
         return localRetorno;
     }
 
     /**
      * @param localRetorno the localRetorno to set
      */
     public void setLocalRetorno(String localRetorno) {
         this.localRetorno = localRetorno;
     }
 }
