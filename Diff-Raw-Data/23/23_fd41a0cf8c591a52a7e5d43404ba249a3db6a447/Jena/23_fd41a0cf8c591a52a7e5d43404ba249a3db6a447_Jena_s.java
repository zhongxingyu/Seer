 
 package dsmw;
 
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.query.Syntax;
 import com.hp.hpl.jena.rdf.model.Literal;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.Statement;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 import com.hp.hpl.jena.tdb.TDBFactory;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.TimeZone;
 
 
 /**
  *
  * @author klm
  */
 public class Jena {
     private String DBdirectory;
     private Model data;
     private String ontoFile;
     
     public static final String rdfUri  = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
     public static final String rdfsUri = "http://www.w3.org/2000/01/rdf-schema#";
     public static final String dsmwUri = "http://www.semanticweb.org/ontologies/2009/4/MS2W.owl#";
     public static final String owlUri  = "http://www.w3.org/2002/07/owl#";
     public static final String xsdUri  = "http://www.w3.org/2001/XMLSchema#";
     public static final String foafUri = "http://xmlns.com/foaf/0.1/";
     public static final String queryPrefix ="prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
 			+"prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                         +"prefix xsd: <http://www.w3.org/2001/XMLSchema#> "
                         +"prefix owl: <http://www.w3.org/2001/XMLSchema#> "
                         +"prefix foaf: <http://xmlns.com/foaf/0.1/> "
 			+"prefix MS2W: <http://www.semanticweb.org/ontologies/2009/4/MS2W.owl#> ";
 
     public Jena(String DB, String onto)
     {
         DBdirectory=DB;
         ontoFile=onto;
         data = TDBFactory.createModel(DBdirectory);
         data.removeAll();
         //data.read(ontoFile,"RDF/XML");
     }
 
     public void close()
     {
         data.close();
     }
 
     public void addStatement(String s, String p, String o)
     {
       Resource subject=data.createResource(s);
       Property predicate = data.createProperty(p);
       Resource object=data.createResource(o);
       Statement st=data.createStatement(subject,predicate,object);
       data.add(st);
     }
 
 
     public void addLiteralStatement(String s, String p, String o)
     {
       Resource subject=data.createResource(s);
       Property predicate = data.createProperty(p);
       Literal object=data.createLiteral(o);
       Statement st=data.createStatement(subject, predicate, object);
       data.add(st);
     }
 
 
 
     public void addSite(Site S)
     {
         this.addStatement(dsmwUri+S.getSiteID(), rdfUri+"type", dsmwUri+"WikiSite");
     }
 
     public void addDocument(Document D)
     {
         this.addStatement(dsmwUri+D.getDocID(), rdfUri+"type", dsmwUri+"Document");
         this.addStatement(dsmwUri+D.getDocID(), dsmwUri+"head", dsmwUri+D.getHead().getPatchID());
         this.addStatement(dsmwUri+D.getDocID(), dsmwUri+"onSite", dsmwUri+D.getSite().getSiteID());
 
     }
 
     public void addOperation(Operation O)
     {
         this.addStatement(dsmwUri+O.getOpID(), rdfUri+"type", dsmwUri+"Operation");
         this.addStatement(dsmwUri+O.getPatch().getPatchID(), dsmwUri+"hasOperation", dsmwUri+O.getOpID());
     }
 
     public void addPatch(Patch P)
     {
         this.addStatement(dsmwUri+P.getPatchID(), rdfUri+"type", dsmwUri+"Patch");
         this.addStatement(dsmwUri+P.getPatchID(), dsmwUri+"onPage", dsmwUri+P.getDoc().getDocID());
         this.addStatement(dsmwUri+P.getPatchID(), dsmwUri+"previous", dsmwUri+P.getPrevious().getPatchID());
         this.addStatement(dsmwUri+P.getChgSet().getChgSetID(), dsmwUri+"hasPatch", dsmwUri+P.getPatchID());
     }
 
     public void addChangeSet(ChangeSet C)
     {
         this.addStatement(dsmwUri+C.getChgSetID(), rdfUri+"type", dsmwUri+"ChangeSet");
         for(Object object : C.getPreviousChgSet())
         {
             String PCS = (String) object;
             if ((!PCS.isEmpty())) this.addStatement(dsmwUri+C.getChgSetID(), dsmwUri+"previousChangeSet", dsmwUri+PCS);
         }
 
         this.addLiteralStatement(dsmwUri+C.getChgSetID(), dsmwUri+"date", C.getDate());
     }
 
     public void publishChangeSet(ChangeSet C)
     {
         this.addLiteralStatement(dsmwUri+C.getChgSetID(), dsmwUri+"published", "true");
     }
 
     public void setPullFeed(ChangeSet C, PullFeed F)
     {
         this.addStatement(dsmwUri+C.getChgSetID(), dsmwUri+"inPullFeed", dsmwUri+F.getPullFeedID());
     }
 
     public void setPushFeed(ChangeSet C, PushFeed F)
     {
         this.addStatement(dsmwUri+C.getChgSetID(), dsmwUri+"inPushFeed", dsmwUri+F.getPushFeedID());
     }
     
     public void addPullFeed(PullFeed PF)
     {
         this.addStatement(dsmwUri+PF.getPullFeedID(), rdfUri+"type", dsmwUri+"PullFeed");
         this.addStatement(dsmwUri+PF.getPullFeedID(), dsmwUri+"hasPullHead", dsmwUri+PF.getHeadPullFeed());
       //  this.addStatement(dsmwUri+PF.getPullFeedID(), dsmwUri+"hasRelatedPush", dsmwUri+PF.getRelatedPushFeed().getPushFeedID());
         this.addStatement(dsmwUri+PF.getSite(), dsmwUri+"hasPull", dsmwUri+PF.getPullFeedID());
 
     }
 
     public void addPushFeed(PushFeed PF)
     {
         this.addStatement(dsmwUri+PF.getPushFeedID(), rdfUri+"type", dsmwUri+"PushFeed");
         this.addStatement(dsmwUri+PF.getPushFeedID(), dsmwUri+"hasPushHead", dsmwUri+PF.getHeadPushFeed());
         this.addStatement(dsmwUri+PF.getSite(), dsmwUri+"hasPush", dsmwUri+PF.getPushFeedID());
     }
 
 
 
     public void loadDataFile(String dataFile)
     {
         System.out.print("Loading the data ... ");
         data.read(dataFile,"N3");
         System.out.println("\tDONE");
     }
 
     public void listSites()
     {
         String query1;
         QueryExecution qe1;
         query1=queryPrefix +
 			"SELECT DISTINCT ?site WHERE { "
 			+"{?site a MS2W:WikiSite} "
 			+"}";
 
         qe1 = QueryExecutionFactory.create(query1, data);
         for (ResultSet rs1 = qe1.execSelect() ; rs1.hasNext() ; )
         {
             QuerySolution binding1 = rs1.nextSolution();
             Resource patch1=((Resource) binding1.get("site"));
             System.out.print(patch1.getURI()+"\n");
         }
         qe1.close();
     }
 
     public void listStatements()
     {
         StmtIterator iter = data.listStatements();
         while (iter.hasNext()) {
             Statement stmt      = iter.nextStatement();  
             Resource  subject   = stmt.getSubject();     
             Property  predicate = stmt.getPredicate();   
             RDFNode   object    = stmt.getObject();      
 
             System.out.print(subject.toString());
             System.out.print(" " + predicate.toString() + " ");
             if (object instanceof Resource) {
                System.out.print(object.toString());
             } else {
                 System.out.print(" \"" + object.toString() + "\"");
             }
             System.out.println(" .");
         }
     }
 
     public ChangeSet getFirstCS()
     {
         ChangeSet CS=null;
         String query1;
         QueryExecution qe1;
         query1=queryPrefix +
 			"SELECT DISTINCT ?cs ?date WHERE { "
 			+"{?cs a MS2W:ChangeSet ."
                         + "?cs MS2W:date ?date ."
                         + "} "
                         +"OPTIONAL { ?cs MS2W:previousChangeSet ?pcs . }"
                         +"FILTER(!bound(?pcs))"
 			+"}";
 
         qe1 = QueryExecutionFactory.create(query1, data);
         for (ResultSet rs1 = qe1.execSelect() ; rs1.hasNext() ; )
         {
             QuerySolution binding1 = rs1.nextSolution();
             Resource chgSet=((Resource) binding1.get("cs"));
             Literal chgSetdate=((Literal) binding1.get("date"));
             CS=new ChangeSet(chgSet.getLocalName());
             CS.setDate(chgSetdate.toString());
         }
         qe1.close();
         return CS;
     }
 
     public ArrayList <ChangeSet> getNextCS(String CS)
     {
         ArrayList <ChangeSet> NCS= new ArrayList<ChangeSet>();
         ChangeSet CSTmp=null;
         String query1;
         QueryExecution qe1;
         query1=queryPrefix +
 			"SELECT DISTINCT ?cs  ?date WHERE { "
 			+" ?cs a MS2W:ChangeSet ."
                         +" ?cs MS2W:date ?date ."
                         +" ?cs MS2W:previousChangeSet MS2W:"+ CS +" . "
 			+" }"
                         +" ORDER BY ?date ";
         qe1 = QueryExecutionFactory.create(query1, data);
         for (ResultSet rs1 = qe1.execSelect() ; rs1.hasNext() ; )
         {
             QuerySolution binding1 = rs1.nextSolution();
             Resource chgSet=((Resource) binding1.get("cs"));
             Literal chgSetdate=((Literal) binding1.get("date"));
             
             CSTmp=new ChangeSet (chgSet.getLocalName());
             CSTmp.addPreviousChgSet(CS);
             CSTmp.setDate(chgSetdate.toString());
             NCS.add(CSTmp);
         }
         qe1.close();
         return NCS;
     }
 
     public ArrayList <ChangeSet> getPreviousCS(String CS)
     {
         ArrayList <ChangeSet> NCS= new ArrayList<ChangeSet>();
         ChangeSet CSTmp=null;
         String query1;
         QueryExecution qe1;
         query1=queryPrefix +
 			"SELECT DISTINCT ?cs  ?date WHERE { "
 			+" ?cs a MS2W:ChangeSet ."
                         +" ?cs MS2W:date ?date ."
                         + "MS2W:"+CS+"  MS2W:previousChangeSet ?cs . "
 			+" }"
                         +" ORDER BY ?date ";
         qe1 = QueryExecutionFactory.create(query1, data);
         for (ResultSet rs1 = qe1.execSelect() ; rs1.hasNext() ; )
         {
             QuerySolution binding1 = rs1.nextSolution();
             Resource chgSet=((Resource) binding1.get("cs"));
             Literal chgSetdate=((Literal) binding1.get("date"));
 
             CSTmp=new ChangeSet (chgSet.getLocalName());
             CSTmp.addPreviousChgSet(CS);
             CSTmp.setDate(chgSetdate.toString());
             NCS.add(CSTmp);
         }
         qe1.close();
         return NCS;
     }
 
 
     public ArrayList <ChangeSet> getCStillDate(Date D)
     {
         ArrayList <ChangeSet> NCS= new ArrayList<ChangeSet>();
         ChangeSet CS;
         String query1;
         String date;
 
         SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
         sdf1.setTimeZone(TimeZone.getTimeZone("GMT"));
         date = sdf1.format(D);
 
         QueryExecution qe1;
         query1=queryPrefix +
 			"SELECT ?cs ?date ?pcs ?pub WHERE { "
 			+" ?cs a MS2W:ChangeSet . "
                         +" ?cs MS2W:date ?date . "
                         +" OPTIONAL { ?cs MS2W:previousChangeSet ?pcs  } ."
                         +" OPTIONAL { ?cs MS2W:published ?pub  } ."
                         +" FILTER ( xsd:dateTime(?date) <= \"" +date+ "\"^^xsd:dateTime )"
 			+" }"
                         +" ORDER BY ?date ";
 
         qe1 = QueryExecutionFactory.create(query1, Syntax.syntaxSPARQL, data);
         for (ResultSet rs1 = qe1.execSelect() ; rs1.hasNext() ; )
         {
             QuerySolution binding1 = rs1.nextSolution();
             Resource chgSet=((Resource) binding1.get("cs"));
             Literal chgSetdate=((Literal) binding1.get("date"));
 
 
             CS=new ChangeSet (chgSet.getLocalName());
             CS.setDate(chgSetdate.toString());
 
             if ((Literal) binding1.get("pub")!=null)
             {
                 Literal chgSetpub=((Literal) binding1.get("pub"));
                 if (chgSetpub.getString().matches("true"))
                     CS.publish();
             }
             if ((Resource) binding1.get("pcs")!=null)
             {
                 Resource chgSetPrev = ((Resource) binding1.get("pcs"));
                 CS.addPreviousChgSet(chgSetPrev.getLocalName());
             }
             boolean newCS = true;
             for (ChangeSet tmpCS:NCS )
             {
                 if (CS.getChgSetID().equals(tmpCS.getChgSetID()))
                 {
                     for (String str :CS.getPreviousChgSet())
                         NCS.get(NCS.indexOf(tmpCS)).addPreviousChgSet(str);
                     newCS=false;
 
                 }
             }
 
             if (newCS)
             {
                 NCS.add(CS);
             }
         }
         qe1.close();
         return NCS;
     }
 
     public void addPushFeeds()
     {
         ChangeSet CS;
         String query1;
 
         QueryExecution qe1;
         query1=queryPrefix +
 			"SELECT DISTINCT ?cs ?date WHERE { "
 			+" ?cs a MS2W:ChangeSet . "
                         +" ?cs MS2W:date ?date . "
 			+" }"
                         +" ORDER BY ?date ";
 
         qe1 = QueryExecutionFactory.create(query1, Syntax.syntaxSPARQL, data);
         for (ResultSet rs1 = qe1.execSelect() ; rs1.hasNext() ; )
         {
             QuerySolution binding1 = rs1.nextSolution();
             Resource chgSet=((Resource) binding1.get("cs"));
             Literal chgSetdate=((Literal) binding1.get("date"));
 
             CS=new ChangeSet (chgSet.getLocalName());
             CS.setDate(chgSetdate.toString());
             ArrayList<ChangeSet> children = this.getNextCS(CS.getChgSetID());
             if (children.size()==2)
             {
                 // push feed
                 String site="S"+CS.getChgSetID().substring(2);
                 Site S = new Site(site);
                 //this.addSite(S);
                 PushFeed PF= new PushFeed("F"+CS.getChgSetID().substring(2));
                 PF.setHeadPushFeed(CS.getChgSetID());
                 PF.setSite(S.getSiteID());
                 this.addPushFeed(PF);
                 this.setPushFeed(CS, PF);
                 ChangeSet NCS=children.get(1);
                 ArrayList<ChangeSet> parents = this.getPreviousCS(NCS.getChgSetID());
                 while ((parents.size()==1)&&(children.size()>0))
                 {
                     this.setPushFeed(NCS, PF);
                     children =this.getNextCS(NCS.getChgSetID());
                     if (children.size()>0) NCS=children.get(0);
                     parents = this.getPreviousCS(NCS.getChgSetID());
                 }
             }
 
         }
         qe1.close();
         
     }
 
 
     public boolean inPushFeed(ChangeSet CS, Date D)
     {
         boolean published=false;
         
         String query1;
         String CSid=CS.getChgSetID();
         
         QueryExecution qe1;
         String date;
 
         SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
         sdf1.setTimeZone(TimeZone.getTimeZone("GMT"));
         date = sdf1.format(D);
 
         while(CSid !=null )
         {
             query1=queryPrefix +
                             "SELECT ?pf  ?date WHERE { "
                             +" ?pf MS2W:hasPushHead MS2W:"+CSid+" ."
                             +" MS2W:"+CSid+" MS2W:date ?date ."
                             +" FILTER ( xsd:dateTime(?date) <= \"" +date+ "\"^^xsd:dateTime )"
                             +"}";
 
             qe1 = QueryExecutionFactory.create(query1, data);
             ResultSet rs1 = qe1.execSelect();
             if (rs1.hasNext())
             {
                 published = true;
                 CSid = null;
             }
             else
             {
                 ArrayList <ChangeSet> next=this.getNextCS(CSid);
                 if (!next.isEmpty()) CSid=next.get(0).getChgSetID();
                 else CSid=null;
             }
             qe1.close();
         }
         return published;
     }
 
     public ChangeSet LowestCommonAncestor(ChangeSet CS1, ChangeSet CS2)
     {
         ChangeSet LCA=null;
         boolean found=false;
 
         while((this.getPreviousCS(CS1.getChgSetID())!=null)&&(!found))
         {
             ArrayList <ChangeSet> parents1=this.getPreviousCS(CS1.getChgSetID());
             ChangeSet tmpCS2=CS2;
             while ((this.getPreviousCS(tmpCS2.getChgSetID()) !=null)&&(!found))
             {
                 ArrayList <ChangeSet> parents2=this.getPreviousCS(tmpCS2.getChgSetID());
                 if (parents1.get(0).equals(parents2.get(0)))
                 {
                     LCA=parents1.get(0);
                     found=true;
                 }
                 if (parents1.get(0).equals(parents2.get(0)))
                 {
                     LCA=parents1.get(0);
                     found=true;
                 }
                 tmpCS2=parents2.get(0);
 
             }
             CS1=parents1.get(0);
         }
 
 
         return LCA;
     }
     public boolean inPullFeed(ChangeSet CS, Date D)
     {
         boolean inPull=false;
 
         String query1;
         String CSid=CS.getChgSetID();
         QueryExecution qe1;
         String date;
 
         SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
         sdf1.setTimeZone(TimeZone.getTimeZone("GMT"));
         date = sdf1.format(D);
 
         while(CSid !=null )
         {
             query1=queryPrefix +
                             "SELECT ?pf  WHERE { "
                             + "?pf MS2W:hasPullHead MS2W:"+CSid+" ."
                             +" MS2W:"+CSid+" MS2W:date ?date ."
                           //  +" FILTER ( xsd:dateTime(?date) <= \"" +date+ "\"^^xsd:dateTime )"
                             + " NOT EXISTS { MS2W:"+CSid+" MS2W:published \"true\".}"
                             +"}";
             
             Query query = QueryFactory.create(query1, Syntax.syntaxARQ);
             qe1 = QueryExecutionFactory.create(query, data);
             ResultSet rs1 = qe1.execSelect();
             if (rs1.hasNext())
             {
                 inPull = true;
                 CSid = null;
             }
             else
             {
                 ArrayList <ChangeSet> next=this.getNextCS(CSid);
                 if (next.size()==1)
                 {
                     CSid = next.get(0).getChgSetID();
                 }
                 else CSid=null;
             }
             qe1.close();
         }
 
         return inPull;
     }
 
     boolean isPullHead(ChangeSet CS, Date D)
     {
         boolean isHead=false;
 
         String query1;
         String CSid=CS.getChgSetID();
         QueryExecution qe1;
         String date;
 
         SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
         sdf1.setTimeZone(TimeZone.getTimeZone("GMT"));
         date = sdf1.format(D);
 
         query1=queryPrefix +
                             "SELECT ?pf  WHERE { "
                             + "?pf MS2W:hasPullHead MS2W:"+CSid+" ."
                             +" MS2W:"+CSid+" MS2W:date ?date ."
                             +" FILTER ( xsd:dateTime(?date) <= \"" +date+ "\"^^xsd:dateTime )"
                           //  + " NOT EXISTS { MS2W:"+CSid+" MS2W:published \"true\".}"
                             +"}";
 
             Query query = QueryFactory.create(query1,Syntax.syntaxARQ);
             qe1 = QueryExecutionFactory.create(query, data);
             ResultSet rs1 = qe1.execSelect();
             if (rs1.hasNext())
                 isHead = true;
 
         return isHead;
     }
 
     void publishParents(ChangeSet CS, Date D)
     {
         String CSid=CS.getChgSetID();
         ArrayList<ChangeSet> parents = this.getPreviousCS(CSid);
         if (this.inPullFeed(parents.get(0),D))
         {
        this.publishChangeSet(parents.get(0));
        parents = this.getPreviousCS(parents.get(0).getChgSetID());
         }
         else 
        {
            this.publishChangeSet(parents.get(1));
            parents = this.getPreviousCS(parents.get(1).getChgSetID());
        }
         while (parents.size()>0)
         {
             this.publishChangeSet(parents.get(0));
             parents = this.getPreviousCS(parents.get(0).getChgSetID());
 
         }
     }
 
     void addsites()
     {
 
     }
 }
