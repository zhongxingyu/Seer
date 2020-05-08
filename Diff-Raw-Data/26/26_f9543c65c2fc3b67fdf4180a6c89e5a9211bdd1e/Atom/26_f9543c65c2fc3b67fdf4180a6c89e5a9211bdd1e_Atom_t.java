 package com.brotherlogic.booser.atom;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 
 /**
  * Base class for all the objects in the system
  * 
  * @author simon
  * 
  */
 public abstract class Atom
 {
    private String dbId;
    private String id;
    private String underlyingRep = "";
 
    /**
     * Constructor
     * 
     * @param atomId
     *           the id of this object
     */
    public Atom(final String atomId)
    {
       id = atomId;
    }
 
    @Override
    public boolean equals(Object obj)
    {
       if (obj instanceof Atom)
          return id.equals(((Atom) obj).id);
 
       return false;
    }
 
    public String getDbId()
    {
       return dbId;
    }
 
    /**
     * Get method for the id of the atom
     * 
     * @return The {@link String} id of the atom
     */
    public String getId()
    {
       return id;
    }
 
    public String getIdName()
    {
       return getClass().getName().replace(".", "_") + "_id";
    }
 
    public String getJson()
    {
       return underlyingRep;
    }
 
    public String getUnderlyingRep()
    {
       return underlyingRep;
    }
 
    public abstract boolean hasDecayed();
 
    protected Object refresh(String name, Class<?> cls)
    {
      // Protect against no underlying rep
      if (getUnderlyingRep() == null || getUnderlyingRep().length() == 0)
         return null;

       JsonObject obj = new JsonParser().parse(getUnderlyingRep()).getAsJsonObject();
       Object retObj = new Gson().fromJson(obj.get(name), cls);
       return retObj;
    }
 
    public void setDbId(String dbId)
    {
       this.dbId = dbId;
    }
 
    public void setId(String id)
    {
       this.id = id;
    }
 
    public void setUnderlyingRep(String rep)
    {
       underlyingRep = rep;
    }
 }
