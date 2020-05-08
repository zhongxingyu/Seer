 /**
  * The contents of this file are subject to the license and copyright
  * detailed in the LICENSE and NOTICE files at the root of the source
  * tree and available online at
  *
  * http://www.dspace.org/license/
  */
 
 package org.dspace.ctask.replicate;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.SQLException;
 import org.apache.log4j.Logger;
 
 import org.dspace.authorize.AuthorizeException;
 import org.dspace.content.Collection;
 import org.dspace.content.Community;
 import org.dspace.content.DSpaceObject;
 import org.dspace.content.ItemIterator;
 import org.dspace.core.ConfigurationManager;
 import org.dspace.core.Constants;
 import org.dspace.curate.AbstractCurationTask;
 import org.dspace.curate.Curator;
 import org.dspace.curate.Suspendable;
 import org.dspace.curate.Utils;
 import org.dspace.pack.Packer;
 import org.dspace.pack.PackerFactory;
 
 /**
  * CompareWithAIP task compares local repository values with the replica store
  * values. It can perform 2 types of comparison: first, an 'integrity' audit
  * which compares the checksums of the local and remote zipped AIPs; second, a 
  * 'count' or enumerative audit that verifies that all child objects of a local 
  * container have corresponding replicas in the remote store.
  * <P>
  * The reason it performs two checks is for performance purposes. We'd rather
  * this task "fail quickly" by finding that a child object is missing from the
  * remote store, than have it require stepping through each child object one-by-one
  * and regenerate each AIP (for checksum verification) before the missing child
  * is located.
  * <P>
  * This task is "suspendable" when invoked from the UI.  This means that if
  * you run an Audit from the UI, this task will return an immediate failure
  * once a single object fails the audit. However, when run from the Command-Line
  * this task will run to completion (i.e. even if an object fails it will continue
  * processing to completion).
  * 
  * @author richardrodgers
  */
 @Suspendable(invoked=Curator.Invoked.INTERACTIVE)
 public class CompareWithAIP extends AbstractCurationTask
 {
     private String archFmt = ConfigurationManager.getProperty("replicate", "packer.archfmt");
     private int status = Curator.CURATE_UNSET;
     private String result = null;
     
     // Group where all AIPs are stored
     private final String storeGroupName = ConfigurationManager.getProperty("replicate", "group.aip.name");
 
     /**
      * Perform 'Compare with AIP' task
      * @param dso DSpace Object to perform on
      * @return integer which represents Curator return status
      * @throws IOException 
      */
     @Override
     public int perform(DSpaceObject dso) throws IOException
     {
         ReplicaManager repMan = ReplicaManager.instance();
         Packer packer = PackerFactory.instance(dso);
         String id = dso.getHandle();
         status = Curator.CURATE_SUCCESS;
         result = "Checksums of local and remote agree";
         String objId = repMan.storageId(id, archFmt);
         try
         {
             //First, make sure this object has an AIP in remote storage
             if(checkReplica(repMan, dso))
             {    
                 // generate an archive and calculate it's checksum
                 File packDir = repMan.stage(storeGroupName, id);
                 File archive = packer.pack(packDir);
                 String chkSum = Utils.checksum(archive, "MD5");
                 // compare with replica
                 String repChkSum = repMan.objectAttribute(storeGroupName, objId, "checksum");
                 if (! chkSum.equals(repChkSum))
                 {
                     report("Local and remote checksums differ for: " + id);
                     report("Local: " + chkSum + " replica: " + repChkSum);
                     result = "Checksums of local and remote differ for: " + id;
                     status = Curator.CURATE_FAIL;
                 }
                 else
                 {
                     report("Local and remote checksums agree for: " + id);
                 }
                 // if a container, also perform an extent (count) audit - i.e.
                 // does replica store have replicas for each object in container?
                 if (Curator.isContainer(dso) || dso.getType() == Constants.SITE)
                 {
                     auditExtent(repMan, dso);
                 }
             }
             setResult(result);
             return status;
         }
         catch (AuthorizeException authE)
         {
             throw new IOException(authE);
         }
         catch (SQLException sqlE)
         {
             throw new IOException(sqlE);
         }
     }
 
     /**
      * Audit the existing contents in the Replica ObjectStore against DSpace object.
      * This method only audits immediate child objects (because child objects of
      * any sub-containers will be audited when 'CompareWithAIP' is called on that
      * container itself).
      * @param repMan ReplicaManager (used to access ObjectStore)
      * @param dso DSpace Object
      * @throws IOException 
      */
     private void auditExtent(ReplicaManager repMan, DSpaceObject dso) throws IOException
     {
         int type = dso.getType();
         
         //If container is a Collection, make sure all Items have AIPs in remote storage
         if (Constants.COLLECTION == type)
         {
             Collection coll = (Collection)dso;
             ItemIterator iter = null;
             try
             {
                 iter = coll.getItems();
                 while (iter.hasNext())
                 {
                     checkReplica(repMan, iter.next());
                 }
             }
             catch (SQLException sqlE)
             {
                 throw new IOException(sqlE);
             }
             finally
             {
                 if (iter != null)
                 {
                     iter.close();
                 }
             }
         } //If Community, make sure all Sub-Communities/Collections have AIPs in remote storage
         else if (Constants.COMMUNITY == type)
         {
             Community comm = (Community)dso;
             try
             {
                 for (Community subcomm : comm.getSubcommunities())
                 {
                     checkReplica(repMan, subcomm);
                 }
                 for (Collection coll : comm.getCollections())
                 {
                     checkReplica(repMan, coll);
                 }
             }
             catch (SQLException sqlE)
             {
                 throw new IOException(sqlE);
             }
         } //if Site, check to see all Top-Level Communities have an AIP in remote storage
         else if (Constants.SITE == type)
         {
             try
             {
                 Community[] topComm = Community.findAllTop(Curator.curationContext());
                 for (Community comm : topComm)
                 {
                     checkReplica(repMan, comm);
                 }
             }
             catch (SQLException sqlE)
             {
                 throw new IOException(sqlE);
             }
         }
     }
 
     /**
      * Check if the DSpace Object already exists in the Replica ObjectStore
      * @param repMan ReplicaManager  (used to access ObjectStore)
      * @param dso DSpaceObject
      * @return true if replica exists, false otherwise
      * @throws IOException 
      */
     private boolean checkReplica(ReplicaManager repMan, DSpaceObject dso) throws IOException
     {
        String objId = repMan.storageId(dso.getHandle(), archFmt); 
         
        if (! repMan.objectExists(storeGroupName, objId))
        {
            String msg = "Missing replica for: " + dso.getHandle();
            report(msg);
            result = msg;
            status = Curator.CURATE_FAIL;
            return false;
        }
        else
            return true;
     }
 }
