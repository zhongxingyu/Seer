 package sgcmf.model.dao;
 
 import java.util.Date;
 import org.hibernate.Query;
 import sgcmf.model.hibernate.Ocorrencia;
 
 public class OcorrenciaDAO extends GeneralDAO<Ocorrencia>
 {
     public Integer queryNumOcorrenciasComInstanteTempoMaior(Short idJogo, Date instanteTempo)
     {
         String hql;
         String resultado;
         
         hql = "select count (o.id) "
                 + "from Ocorrencia o " +
                "where o.instantetempo > :it";
         Query q = sessao.createQuery(hql);
         q.setParameter("it", instanteTempo);
         
         resultado = String.valueOf(q.uniqueResult());
         return Integer.parseInt(resultado);
     }
 }
