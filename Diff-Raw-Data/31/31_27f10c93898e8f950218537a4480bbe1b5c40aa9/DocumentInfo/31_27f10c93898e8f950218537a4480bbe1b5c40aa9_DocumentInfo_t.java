 package org.jcouchdb.document;
 
 import org.svenson.JSONProperty;
 
 /**
  * Provides information about created or updated documents.
  *
  * @author shelmberger
  *
  */
 public class DocumentInfo
 {
    private String id, revision, error, reason;
 
     private boolean ok;
 
     public String getId()
     {
         return id;
     }
 
     public void setId(String id)
     {
         this.id = id;
     }
 
     @JSONProperty("rev")
     public String getRevision()
     {
         return revision;
     }
 
     public void setRevision(String revision)
     {
         this.revision = revision;
     }
 
     public boolean isOk()
     {
         return ok;
     }
 
     public void setOk(boolean ok)
     {
         this.ok = ok;
     }
 
     public String getError()
     {
         return error;
     }
 
     public void setError(String error)
     {
         if (error != null)
         {
             this.ok = false;
         }
         this.error = error;
     }
 
    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }
    
     @Override
     public String toString()
     {
        return super.toString()+": id = "+id+", revision = "+revision+(ok ? ", ok = true" : ", ok = false, error = "+error+", reason = " + reason);
     }
 }
