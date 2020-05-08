 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editorepo.
  */
 package com.albertdelafuente.eapiterator;
 import org.sparx.*;
 import java.lang.*;
 import static java.lang.System.out;
 
 /**
  *
  * @author afu
  */
 public class eapIterator {
     private String inputFile;
     private org.sparx.Repository repo;
     
     eapIterator() {
         inputFile = "";
         repo = new org.sparx.Repository();
         out.println("constructor");
     }
 
     void openRepository() {
         if (repo.OpenFile(inputFile)) {
             out.println("b is true");
         }
     }
 
     void listRepositoryInfo() {
         out.println("-- Repostitory --");
         //out.println("Models Count: " + repo.GetModels().GetCount());
         //out.println("Diagrams Count: " + repo.GetModels().GetCount());
         out.println("Instance GUID: " + repo.GetInstanceGUID()); // Earepository.InstanceGUID
         out.println("ConnectionString: " + repo.GetConnectionString());
         out.println("Library version: " + repo.GetLibraryVersion());
         out.println("Model count: " + repo.GetModels().GetCount());
         out.println("Terms count: " +  repo.GetTerms().GetCount());
         out.println("Issues count: " + repo.GetIssues().GetCount());
         out.println("Author count: " + repo.GetAuthors().GetCount());
         out.println("Client count: " + repo.GetClients().GetCount());
         out.println("Task count: " + repo.GetTasks().GetCount());
         out.println("Datatypes count: " + repo.GetDatatypes().GetCount());
         out.println("Recource count: " + repo.GetResources().GetCount());
         out.println("Stereotype count: " + repo.GetStereotypes().GetCount());
         out.println("PropertyType count: " + repo.GetPropertyTypes().GetCount());
     }
     
     void listModels(int indentLevel, org.sparx.Repository EaRepository){
         short idx;
         org.sparx.Package i;
         String tab = new String(new char[indentLevel]).replace('\0', ' ');
         
         out.printf("%s-- Models [%d] (a package) --\n", tab, EaRepository.GetModels().GetCount());
         for(idx=0; idx<EaRepository.GetModels().GetCount(); idx++){
 	   i = EaRepository.GetModels().GetAt(idx);
 	   out.printf("%sModel #%d ---\n", tab, idx);
 	   out.printf("%sName: %s, PackageID: %s, PackageGUID %s\n", tab, i.GetName(), i.GetPackageID(), i.GetPackageGUID());
 	   out.printf("%sCreated: %s, Modified: %s, Version: %s\n", tab, i.GetCreated(), i.GetModified(), i.GetVersion());
 	   out.printf("%sIsNamespace: %b, IsControlled: %b\n", tab, i.GetIsNamespace(), i.GetIsControlled());
 	   out.printf("%sIsProtected %b, IsModel: %b\n", tab, i.GetIsProtected(), i.GetIsModel());
 	   out.printf("%sUseDTD: %b, LogXML: %b, XMLPath: %s\n", tab, i.GetUseDTD(), i.GetLogXML(), i.GetXMLPath());
 	   out.printf("%sLastLoadDate: %s, LastSaveDate: %s\n", tab, i.GetLastLoadDate(), i.GetLastSaveDate());
 	   out.printf("%sOwner: %s, CodePath: %s\n", tab, i.GetOwner(), i.GetCodePath());
 	   out.printf("%sUMLVersion: %s, TreePos: %s\n", tab, i.GetUMLVersion(), i.GetTreePos());
 	   out.printf("%sElement: %s, IsVersionControlled: %s\n", tab, i.GetElement(), i.GetIsVersionControlled());
 	   out.printf("%sBatchLoad: %d, BatchSave: %d\n", tab, i.GetBatchLoad(), i.GetBatchSave());
 	   out.printf("%sNotes: %s\n", tab, i.GetNotes());
 	   out.printf("%sPackage count: %d\n", tab, i.GetPackages().GetCount());
 	   out.printf("%sElement count: %d\n", tab, i.GetElements().GetCount());
 	   out.printf("%sDiagram count: %d\n", tab, i.GetDiagrams().GetCount());
 	   out.printf("%sConnector count: %d\n", tab, i.GetConnectors().GetCount());
            out.printf("%s---\n", tab, idx);
         }
     }
 
     void listPackages(int indentLevel, org.sparx.Package root) {
         short idx;
         org.sparx.Package i;
         String tab = new String(new char[indentLevel]).replace('\0', ' ');
         
         out.printf("%s-- Packages [%d] --\n", tab, root.GetPackages().GetCount());
         for (idx=0; idx<root.GetPackages().GetCount(); idx++) {
    	   i = root.GetPackages().GetAt(idx);
 	   out.printf("%sPackage #%d\n", tab, idx);
            out.printf("%sPackage id: %d\n", tab, i.GetPackageID());
 	   out.printf("%sName: %s\n", tab, i.GetName());
         }
     }
     
     void listDiagrams(int indentLevel, org.sparx.Package root) {
         short idx;
         org.sparx.Diagram i;
         String tab = new String(new char[indentLevel]).replace('\0', ' ');
         
         out.printf("%s-- Diagrams [%d] --\n", tab, root.GetDiagrams().GetCount());
         for (idx=0; idx<root.GetDiagrams().GetCount(); idx++) {
    	   i = root.GetDiagrams().GetAt(idx);
 	   out.printf("%sDiagram #%d\n", tab, idx);
            out.printf("%sGUID id: %s\n", tab, i.GetDiagramGUID());
 	   out.printf("%sName: %s\n", tab, i.GetName());
         }
     }
     
     void listElements(int indentLevel, org.sparx.Package root) {
         short idx;
         org.sparx.Element i;
         String tab = new String(new char[indentLevel]).replace('\0', ' ');
         
         out.printf("%s-- Diagrams [%d] --\n", tab, root.GetElements().GetCount());
         for (idx=0; idx<root.GetElements().GetCount(); idx++) {
    	   i = root.GetElements().GetAt(idx);
 	   out.printf("%sElement #%d\n", tab, idx);
            out.printf("%sGUID id: %s\n", tab, i.GetElementGUID());
 	   out.printf("%sName: %s\n", tab, i.GetName());
         }
     }
 
     void listSelected(org.sparx.Package root) {
         short i;
         org.sparx.Element e;
 
         for (i=0; i<root.GetElements().GetCount(); i++) {
    	   e = root.GetElements().GetAt(i);
 	   out.printf("Element #%d\n", i);
            //out.printf("ID: %d\n", x.GetPackageID());
 	   out.printf("Name: %s\n", e.GetName());   
         }
     }
     
     String subAlias(String s){
         String alias = s.substring(0, Math.min(s.length(), 7));
         return alias;
     }
     
     void relatewfrfi() {
         short i, j;
         org.sparx.Package reqp, wfp, root;
         org.sparx.Element req, wf;
         
         out.println("-- Packages  --");
         //listSubPackages(repo.GetPackageByID(0));
         reqp = repo.GetModels().GetByName("SIGA stable");
         reqp = reqp.GetPackages().GetByName("Biblioteca de Requisitos (RFI / RFN / RNF / RN)");
         reqp = reqp.GetPackages().GetByName("Requisitos Funcionais de Interface (RFI)");
         reqp = reqp.GetPackages().GetByName("Comum - Requisitos Funcionais de Interface (RFI)");
         //listPackages(4, reqp);
         //listDiagrams(6, reqp);
         //listElements(4, reqp);
         
         wfp = repo.GetModels().GetByName("SIGA stable");
         wfp = wfp.GetPackages().GetByName("Biblioteca de Interfaces");
         wfp = wfp.GetPackages().GetByName("test");
         //listElements(6, wfp);
         
         for (i=0; i<wfp.GetElements().GetCount(); i++) {
    	    wf = wfp.GetElements().GetAt(i);
             for (j=0; j<reqp.GetElements().GetCount(); j++) {
                 req = reqp.GetElements().GetAt(j);
 
                 out.printf("wf(%d) = %s\n", i, wf.GetName());
                 out.printf("wf(%d) = %s\n", i, subAlias(wf.GetName()));
                 out.printf("req(%d) = %s\n", j, req.GetName());
                 out.printf("req(%d) = %s\n", j, subAlias(req.GetName()));
                 
                 if (subAlias(req.GetAlias()).equals(subAlias(wf.GetAlias()))) {
                     out.printf("Creating relation between:\n");
                     out.printf("  Element #%s\n", wf.GetElementGUID());
                     out.printf("  Element #%s\n", req.GetElementGUID());
 //                    wf.GetConnectors().AddNew(inputFile, inputFile);
                    
                    //Connector addNew = wf.GetConnectors().AddNew("name", "UseCase"),
                    Connector addNew = wf.GetConnectors().AddNew(subAlias(req.GetAlias()), "Realization");
                     addNew.SetSupplierID(req.GetElementID());
                     addNew.Update();
                     wf.Refresh();
 
                     
 //                    Connector addNew = e1.GetConnectors().AddNew("moj usecase",  "UseCase");
 //                    addNew.SetSupplierID(e2.GetElementID());
 //                    addNew.Update();
 //                    e1.Refresh();
 //                    
 //                    EA.Connector newConnector = Element.Connectors.AddNew("""Realization");
 //                    newConnector.SupplierID = myTargetID;
 //                    newConnector.Update(); 
                 }
             }
         }
 
     }
     
     public void run (String[] args) throws Exception {
        // Parameters example
        // $0 -i eap -l -option(-all -guid) /root/model/submodel -o file -v 3
        // $0 -i eap -o file
         inputFile = "C:\\eap-iterator\\siga.eap";
         openRepository();
         listRepositoryInfo();
         listModels(2, repo);
         relatewfrfi();
         //listSubPackages(4, repo.GetModels().GetAt((short) 0));
         //listSubPackages(4, repo.GetModels().GetByName("SIGA stable"));
         //listSubPackages(6, repo.GetModels().GetAt((short) 0).GetPackages().GetAt((short) 1));
         //listSubPackages(6, repo.GetModels().GetAt((short) 0).GetPackages().GetAt((short) 1).GetPackages().GetAt((short) 1));
         //listSubPackages(repo.getp);
         //relatewfrfi();
     }
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         try {
             eapIterator eai = new eapIterator();
             eai.run(args);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
 
 //    /// <summary>
 //    /// adds all elements in the model to the cache
 //    /// </summary>
 //    public void addAllElementsToCache(string startingPackageID)
 //    {
 //		//first check if we are dealing with a local eap file in MS Access format.
 //		//in that case we cannot perform the query with the unions
 //		bool localEAPFile = true;
 //		String connection = this.wrappedModel.ConnectionString;
 //		if (connection.ToLower().EndsWith( ".eap"))
 //		{
 //		    //could be an eap file, or could be a shortcut file.
 //		    //figure it out based on the size of the file
 //		    try
 //		    {
 //		    FileInfo connectionInfo = new FileInfo(connection);
 //		    if (connectionInfo.Length > 1000)
 //		    {
 //			  localEAPFile = true;
 //		    }
 //		    else
 //		    {
 //			  localEAPFile = false;
 //		    }
 //
 //		    }catch (Exception)
 //		    {
 //			  localEAPFile = false;
 //		    }
 //		}
 //		Loggerepo.log("start caching elements");
 //		//build the sql string to get all the object fromthe starting package and 9 levels of packages underneath.
 //		//there is a better way using a recursive query, but these are not supported in sql 2000 yet (only from sql 2005)
 //		string sqlGetElements = @" SELECT o.Object_ID FROM t_object as o
 //						left join t_package p on o.Package_ID = p.Package_ID
 //						left join t_package p2 on p.Parent_ID = p2.Package_ID
 //						left join t_package p3 on p2.Parent_ID = p3.Package_ID
 //						left join t_package p4 on p3.Parent_ID = p4.Package_ID
 //						left join t_package p5 on p4.Parent_ID = p5.Package_ID
 //						left join t_package p6 on p5.Parent_ID = p6.Package_ID
 //						left join t_package p7 on p6.Parent_ID = p7.Package_ID
 //						left join t_package p8 on p7.Parent_ID = p8.Package_ID
 //						left join t_package p9 on p8.Parent_ID = p9.Package_ID
 //
 //						where
 //						p.package_ID = " + startingPackageID + @"
 //						    or p2.package_ID = " + startingPackageID + @"
 //						    or p3.package_ID = " + startingPackageID + @"
 //						    or p4.package_ID = " + startingPackageID + @"
 //						    or p5.package_ID = " + startingPackageID + @"
 //						    or p6.package_ID = " + startingPackageID + @"
 //						    or p7.package_ID = " + startingPackageID + @"
 //						    or p8.package_ID = " + startingPackageID + @"
 //						    or p9.package_ID = " + startingPackageID;
 //		try
 //		{
 //		    if (!localEAPFile)
 //		    {
 //			  //we only do caching when working on an remote database
 //			  EAWrapperFactory.createEAWrappers(this, this.wrappedModel.GetElementSet(sqlGetElements, 2));
 //		    }
 //		    Loggerepo.log("end caching elements");
 //		}
 //		catch (Exception)
 //		{
 //		    // oh well, caching failed, no biggy, we can work without caching, its just a bit slower
 //		    Loggerepo.logError("Caching failed");
 //		}
 //	  } 
