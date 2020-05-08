 package main.web;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 import org.persistence.INTSIDENT;
 import org.persistence.ISIK_INTSIDENDIS;
 import org.persistence.OBJEKT;
 import org.persistence.OBJEKT_INTSIDENDIS;
 import org.persistence.PIIRIRIKKUJA;
 import org.persistence.PIIRIVALVUR_INTSIDENDIS;
 import org.persistence.VAHTKOND;
 import org.persistence.VAHTKOND_INTSIDENDIS;
 import org.springframework.beans.factory.annotation.Configurable;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 
 @Configurable
 @RooJavaBean
 public class IntsidendigaSeotudElem {
 	@PersistenceContext
 	EntityManager entityManager;
 	
 	public IntsidendigaSeotudElem()
 	{}
 
 	public static final EntityManager entityManager() {
 		EntityManager em = new IntsidendigaSeotudElem().entityManager;
 		if (em == null)
 			throw new IllegalStateException(
 					"Entity manager has not been injected");
 		return em;
 	}
 
     @SuppressWarnings({ "unchecked", "deprecation" })
     public static List<PIIRIRIKKUJA>GetIntsidendigaSeotudAktivPiiririkkujad(INTSIDENT intsident)
        {
         Query query = entityManager().createNativeQuery(
                "SELECT * from T_ISIK_INTSIDENDIS WHERE suletud > CURRENT_DATE and T_ISIK_INTSIDENDIS.iNTSIDENT_ID = " + intsident.getIntsidentId(), ISIK_INTSIDENDIS.class);       
            List<ISIK_INTSIDENDIS> isikudIntsidendis = query.getResultList();
            if(isikudIntsidendis == null || isikudIntsidendis.isEmpty())
            { return null; }
 
      List<PIIRIRIKKUJA> tulemList = new ArrayList<PIIRIRIKKUJA>();
            for(ISIK_INTSIDENDIS isin : isikudIntsidendis)
            {
             PIIRIRIKKUJA rikkuja = isin.getPIIRIRIKKUJA_ID();
             if(rikkuja != null)
             { tulemList.add(rikkuja); }
            }
         return tulemList;
        }
 
     @SuppressWarnings({ "unchecked", "deprecation" })
 	public static List<VAHTKOND>GetIntsidendigaSeotudAktivVahtkonnad(INTSIDENT intsident)
     {
     	Query query = entityManager().createNativeQuery(
        	    "SELECT * from T_VAHTKOND_INTSIDENDIS WHERE suletud > CURRENT_DATE and T_VAHTKOND_INTSIDENDIS.iNTSIDENT_ID = " + intsident.getIntsidentId(), VAHTKOND_INTSIDENDIS.class);       
         List<VAHTKOND_INTSIDENDIS> vahtkonnnadIntsidendis =  query.getResultList();
         if(vahtkonnnadIntsidendis == null || vahtkonnnadIntsidendis.isEmpty())
         { return null; }
 
 		List<VAHTKOND> tulemList = new ArrayList<VAHTKOND>();
         for(VAHTKOND_INTSIDENDIS vahin : vahtkonnnadIntsidendis)
         {
         	VAHTKOND vahtkond = vahin.getVAHTKOND_ID();
         	if(vahtkond != null)
         	{ tulemList.add(vahtkond); }
         }
     	return tulemList;
     }
 	
     @SuppressWarnings({ "unchecked", "deprecation" })
 	public static List<OBJEKT>GetIntsidendigaSeotudAktivObjektid(INTSIDENT intsident)
     {
     	Query query = entityManager().createNativeQuery(
        	    "SELECT * from T_OBJEKT_INTSIDENDIS WHERE suletud > CURRENT_DATE and T_OBJEKT_INTSIDENDIS.iNTSIDENT_ID = " + intsident.getIntsidentId(), OBJEKT_INTSIDENDIS.class);       
         List<OBJEKT_INTSIDENDIS> objektidIntsidendis =  query.getResultList();
         if(objektidIntsidendis == null || objektidIntsidendis.isEmpty())
         { return null; }
 
 		List<OBJEKT> tulemList = new ArrayList<OBJEKT>();
         for(OBJEKT_INTSIDENDIS obin : objektidIntsidendis)
         {
         	OBJEKT objekt = obin.getOBJEKT_ID();
         	if(objekt != null)
         	{ tulemList.add(objekt); }
         }
     	return tulemList;
     }
 	
     @SuppressWarnings({ "unchecked", "deprecation" })
 	public static List<PIIRIVALVUR_INTSIDENDIS>GetIntsidendigaSeotudAktivPiirivalvuriIntsidendid(INTSIDENT intsident)
     {
     	Query query = entityManager().createNativeQuery(
        	    "SELECT * from T_PIIRIVALVUR_INTSIDENDIS WHERE suletud > CURRENT_DATE and T_PIIRIVALVUR_INTSIDENDIS.iNTSIDENT_ID = " + intsident.getIntsidentId(), PIIRIVALVUR_INTSIDENDIS.class);       
 		List<PIIRIVALVUR_INTSIDENDIS> piirivalvuridIntsidendis =  query.getResultList();
         if(piirivalvuridIntsidendis == null || piirivalvuridIntsidendis.isEmpty())
         { return null; }
 
     	return piirivalvuridIntsidendis;
     }
 }
