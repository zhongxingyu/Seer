 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package br.gov.saudecaruaru.bpai.data;
 
 import br.gov.saudecaruaru.bpai.business.model.BIProcedimentoRealizado;
 import br.gov.saudecaruaru.bpai.business.model.ProcedimentoRealizado;
 import br.gov.saudecaruaru.bpai.business.model.ProcedimentoRealizadoPK;
 import java.util.ArrayList;
 import java.util.List;
 import org.hibernate.Query;
 import org.hibernate.Session;
 
 /**
  *
  * @author Albuquerque
  */
 public class BIProcedimentoRealizadoDAO extends GenericDAO<BIProcedimentoRealizado> {
 
     public BIProcedimentoRealizadoDAO() {
         //super(EntityManagerUtil.getEntityManagerI());
         
     }
     public String getNextFolha(BIProcedimentoRealizado BIProcedimentoRealizado){
          Session session= this.getSession();
          List l=null;
         try{     
             StringBuilder sql=new StringBuilder();
             //campos a serem selecionados
             sql.append("select MAX(pro.biProcedimentoRealizadoPK.numeroFolha)");
             sql.append(" FROM BIProcedimentoRealizado pro");
             //traz somente os procedimentos que devem ser consolidados
             sql.append(" WHERE (pro.biProcedimentoRealizadoPK.cnesUnidade=:cnesUnidade)");
             sql.append(" AND (pro.biProcedimentoRealizadoPK.competencia=:competencia)");
             sql.append(" AND (pro.biProcedimentoRealizadoPK.cboMedico=:cbo)");
             sql.append(" AND (pro.biProcedimentoRealizadoPK.cnsMedico=:cnsMedico)");
             
             //it's create query
             Query q=session.createQuery(sql.toString());
             q.setParameter("cnesUnidade", BIProcedimentoRealizado.getBiProcedimentoRealizadoPK().getCnesUnidade());
             q.setParameter("competencia",  BIProcedimentoRealizado.getBiProcedimentoRealizadoPK().getCompetencia());
             q.setParameter("cbo",  BIProcedimentoRealizado.getBiProcedimentoRealizadoPK().getCboMedico());
             q.setParameter("cnsMedico",  BIProcedimentoRealizado.getBiProcedimentoRealizadoPK().getCnsMedico());
             
             l=q.list();
             String f = "";
             String folha = null;
             for(Object row:l){
                folha = (String)row;
             }
             if(folha!=null){
                 int nextFolha = (Integer.parseInt(folha)+1);
                 f = String.format("%03d",nextFolha);
             }
             return f;
         }catch(Exception ex){
             ex.printStackTrace();
             return "";
         }
     }
     /**
      * Pega todos os procedimentos consolidados de forma páginada, mas de forma agrupada pelos seguintes campos:
      * Cnes da unidade, CBO do profissional, Idade do paciente, código do procedimento e competência
      * @param competencia - competência dos registros
      * @param firstResult - o index do primeiro registro
      * @param maxResult -  a quantidade máxima de resultados resultados
      * @return  Lista de com os procedimentos realizados
      */
     public List<ProcedimentoRealizado> findAllConsolidados(String competencia,int firstResult,int maxResult){
         List<Object[]> l=null;
         Session session= this.getSession();
         try{
 //            example in SQL for get the procedimentos
 //            ===> first X skip Y equivalent in Mysql at limit Y,X <===
 //            select first 100 skip 50 pr.prd_cbo,pr.prd_uid,pr.prd_idade,p.pa_dc,p.pa_id,pr.prd_org,pr.prd_mvm,pr.prd_cmp, sum(pr.prd_qt_p) 
 //            FROM s_prd pr inner join S_PA p on (p.pa_id||p.pa_dv=pr.prd_pa and  p.pa_idebpa='S' ) 
 //            group by pr.prd_uid,pr.prd_cbo, p.pa_id,pr.prd_idade, p.pa_dc,pr.prd_org,pr.prd_cmp,pr.prd_mvm;
 
             StringBuilder sql=new StringBuilder();
             //campos a serem selecionados
             sql.append("SELECT  pro.biProcedimentoRealizadoPK.cnesUnidade, pro.biProcedimentoRealizadoPK.cboMedico,");
             sql.append("pro.idadePaciente, pro.codigoProcedimento, pro.biProcedimentoRealizadoPK.competencia,");
             //faz o somatório da quantidade de execuções
             sql.append("pro.prdMvm, SUM(pro.quantidadeRealizada) AS quantidadeRealizada");
             sql.append(" FROM BIProcedimentoRealizado pro");
             //traz somente os procedimentos que devem ser consolidados
             sql.append(" WHERE (pro.origemProcedimento=:origem)");
             //agrupa
             sql.append(" GROUP BY pro.biProcedimentoRealizadoPK.cnesUnidade, pro.biProcedimentoRealizadoPK.cboMedico,");
             sql.append(" pro.codigoProcedimento,pro.idadePaciente,pro.biProcedimentoRealizadoPK.competencia,pro.prdMvm");
             //it's create query
             Query q=session.createQuery(sql.toString());
             q.setParameter("origem", ProcedimentoRealizado.ORIGEM_CONSOLIDADO);
             //paginacao dos resultados
             q.setFirstResult(firstResult);
             q.setMaxResults(maxResult);
             l=q.list();
         }catch(Exception ex){
             ex.printStackTrace();
         }
         finally{
             //session.flush();
             session.close();
             List<ProcedimentoRealizado> list= new ArrayList<ProcedimentoRealizado>();
             if(l!=null){
                 //cada objeto de l contém um vetor que representa os campos selecionados
                 for(Object[] row:l){
                     ProcedimentoRealizado pro=new ProcedimentoRealizado(new ProcedimentoRealizadoPK());
                     
                     //o tamanho do vetor é igual a quantidade de campos do select
                     //o índíce do campo no select é igual ao do vetor, começando por zero.
                     pro.getProcedimentoRealizadoPK().setCnesUnidade((String)row[0]);
                     pro.getProcedimentoRealizadoPK().setCboMedico((String)row[1]);
                     pro.setIdadePaciente((String)row[2]);
                     pro.setCodigoProcedimento((String)row[3]);
                     pro.getProcedimentoRealizadoPK().setCompetencia((String)row[4]);
                     pro.setPrdMvm((String)row[5]);
                     pro.setQuantidadeRealizada((Double) row[6]);
                     
                     //valores padrões
                     
                     pro.setOrigemProcedimento(ProcedimentoRealizado.ORIGEM_CONSOLIDADO);
                     pro.setNomePaciente("TESTE");
                     String flag="0";
                     pro.setPrdFlca(flag);
                     pro.setPrdFlcbo(flag);
                     pro.setPrdFlcid(flag);
                     pro.setPrdFler(flag);
                     pro.setPrdFlida(flag);
                     pro.setPrdFlmun(flag);
                     pro.setPrdFlpa(flag);
                     pro.setPrdFlqt(flag);
                     
                     list.add(pro);
                 }
                 l.clear();
             }
             return list;
         }
     }
     
     /**
      * Pega todos os procedimentos realizados que são individuais de forma páginada
      * @param competencia - competência dos registros
      * @param firstResult - o index do primeiro registro
      * @param maxResult -  a quantidade máxima de resultados resultados
      * @return  Lista de com os procedimentos realizados
      */
     public List<ProcedimentoRealizado> findAllProcedimentosIndividuais(String competencia,int firstResult,int maxResult){
         List<ProcedimentoRealizado> list=new ArrayList<ProcedimentoRealizado>();
         List<BIProcedimentoRealizado> l=null;
         Session session= this.getSession();
         try{
 
             StringBuilder sql=new StringBuilder();
             //campos a serem selecionados
            sql.append("SELECT *");
             //faz o somatório da quantidade de execuções
             sql.append(" FROM BIProcedimentoRealizado pro");
             //traz somente os procedimentos que devem ser consolidados
             sql.append(" WHERE (pro.origemProcedimento=:origem)");
             //it's create query
             Query q=session.createQuery(sql.toString());
             q.setParameter("origem", ProcedimentoRealizado.ORIGEM_INDIVIDUALIZADO);
             //paginacao dos resultados
             q.setFirstResult(firstResult);
             q.setMaxResults(maxResult);
             l=q.list();
         }catch(Exception ex){
             ex.printStackTrace();
         }
         finally{
             //session.flush();
             session.close();
             if(l!=null){
                 //cada objeto de l contém um vetor que representa os campos selecionados
                 for(BIProcedimentoRealizado row: l){
                     ProcedimentoRealizado pro=new ProcedimentoRealizado(row);
                     list.add(pro);
                 }
                 l.clear();
             }
             return list;
         }
     
     }
     
     @Override
     public Session getSession() {
         return HibernateUtil.getSessionBpaI();
     }
     
     /**
     * Devolve todos os procedimentos somente com os campos d cabeçalho preenchido.
     * A saber: CNES da Unidade, CBO do profissional, Número da Folha, Competência e CNS do médico.
     * Em outras palavaras: agrupa os resgistros pelos campos descritos acima.
     * @return List<ProcedimentoRealizado> - A lista de todos os procedimentos encontrados
     */
    public List<ProcedimentoRealizado> findAllOnlyHeader(){
         List<Object[]> l=null;
         Session session= this.getSession();
         try{
 
             StringBuilder sql=new StringBuilder();
             //campos a serem selecionados
             sql.append("SELECT  pro.biProcedimentoRealizadoPK.cnesUnidade, pro.biProcedimentoRealizadoPK.cboMedico,");
             sql.append(" pro.biProcedimentoRealizadoPK.numeroFolha, pro.biProcedimentoRealizadoPK.competencia,");
             //faz o somatório da quantidade de execuções
             sql.append("pro.biProcedimentoRealizadoPK.cnsMedico, COUNT(pro.biProcedimentoRealizadoPK.sequenciaFolha) AS VOID");
             sql.append(" FROM BIProcedimentoRealizado pro");
             //traz somente os procedimentos que devem ser consolidados
             //sql.append(" WHERE (pro.origemProcedimento=:origem)");
             //agrupa
             sql.append(" GROUP BY pro.biProcedimentoRealizadoPK.competencia, pro.biProcedimentoRealizadoPK.cnesUnidade,");
             sql.append(" pro.biProcedimentoRealizadoPK.cnsMedico,pro.biProcedimentoRealizadoPK.cboMedico,pro.biProcedimentoRealizadoPK.numeroFolha");
             //it's create query
             Query q=session.createQuery(sql.toString());
             //paginacao dos resultados
             l=q.list();
         }catch(Exception ex){
             ex.printStackTrace();
         }
         finally{
             List<ProcedimentoRealizado> list= new ArrayList<ProcedimentoRealizado>();
             if(l!=null){
                 //cada objeto de l contém um vetor que representa os campos selecionados
                 for(Object[] row:l){
                     ProcedimentoRealizado pro=new ProcedimentoRealizado(new ProcedimentoRealizadoPK());
                     
                     //o tamanho do vetor é igual a quantidade de campos do select
                     //o índíce do campo no select é igual ao do vetor, começando por zero.
                     pro.getProcedimentoRealizadoPK().setCnesUnidade((String)row[0]);
                     pro.getProcedimentoRealizadoPK().setCboMedico((String)row[1]);
                     pro.getProcedimentoRealizadoPK().setNumeroFolha((String)row[2]);
                     pro.getProcedimentoRealizadoPK().setCompetencia((String)row[3]);
                     pro.getProcedimentoRealizadoPK().setCnsMedico((String)row[4]);
                     
                     list.add(pro);
                 }
             }
             session.close();
             return list;
         }
     }
     
 }
