 package edu.mit.cci.simulation.client.model.transitional;
 
 import edu.mit.cci.simulation.client.MetaData;
 import edu.mit.cci.simulation.client.comm.RepositoryManager;
 import edu.mit.cci.simulation.model.DataType;
 import edu.mit.cci.simulation.model.Variable;
 
 /**
  * User: jintrone
  * Date: 3/17/11
  * Time: 4:17 PM
  */
 public class AdaptedMetaData extends AdaptedObject<Variable> implements MetaData {
 
     private VarContext vc;
     private VarType vt;
     private Class<Object>[] profile;
     private String[] units;
     private String[] max;
     private String[] min;
     private String[] labels;
 
     private boolean isIndex = false;
 
     public AdaptedMetaData(Variable o, RepositoryManager manager) {
         super(o, manager);
     }
 
     @Override
     public Long getId() {
         return model().getId();
     }
 
     @Override
     public MetaData getIndexingMetaData() {
         return (MetaData) manager().getAdaptor(model().getIndexingVariable());
     }
 
     @Override
     public void setIndexingMetaData(MetaData md) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public String getName() {
         return model().getName();
     }
 
     @Override
     public void setName(String name) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public String getInternalName() {
         return model().getExternalName();
     }
 
     @Override
     public void setInternalName(String name) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public String[] getUnits() {
     	if (units == null) {
             Variable v = model().getIndexingVariable();
             if (v != null) {
             	units = new String[] {v.getUnits(), model().getUnits()};
             }
             else {
             	units = new String[] { model.getUnits() };
             }
     	}
         return units; 
     }
 
     @Override
     public void setUnits(String[] units) {
     	this.units = units;
     }
 
     @Override
     public Class<Object>[] getProfile() {
         if (profile == null) {
             Variable v = model().getIndexingVariable();
             if (v != null) {
                 profile = new Class[]{mapClass(v.getDataType(),v.getPrecision_()), mapClass(model().getDataType(), model().getPrecision_())};
             } else {
                 profile = new Class[]{mapClass(model().getDataType(), model().getPrecision_())};
             }
         }
         return profile;
     }
 
     @Override
     public void setProfile(Class<Object>[] profile) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public String[] getLabels() {
         if (labels == null) {
             Variable v = model().getIndexingVariable();
             if (v != null) {
                labels = new String[]{v.getIndexingVariable().getLabels(), model().getLabels() };
             }
             else {
                 labels = new String[]{ model().getLabels() };
             }
             
         }
         return labels;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void setLabels(String[] labels) {
         this.labels = labels;
     }
 
     @Override
     public String[] getMax() {
         if (max == null) {
           Variable v = model().getIndexingVariable();
             if (v != null) {
                 max = new String[] {v.getMax_()+"", model().getMax_()+""};
             } else {
                 max = new String[] {model().getMax_()+""};
             }
             
 
         }
         return max;
     }
 
     @Override
     public void setMax(String[] n) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public String[] getMin() {
         if (min == null) {
           Variable v = model().getIndexingVariable();
             if (v != null) {
                 min = new String[] {v.getMin_()+"", model().getMin_()+""};
             } else {
                 min = new String[] {model().getMin_()+""};
             }
             
 
         }
         return min;
     }
 
     @Override
     public void setMin(String[] n) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public String[] getDefault() {
        return getMin();
     }
 
     @Override
     public void setDefault(String[] n) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public String[] getCategories() {
         return model().getOptions();
     }
 
     @Override
     public String getDescription() {
         return model().getDescription();
     }
 
     @Override
     public void setDescription(String desc) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public VarContext getVarContext() {
         if (vc == null) {
             Variable v = model();
 
             if (v.getIndexingVariable() != null) {
                 if (v.getIndexingVariable().getId().equals(model.getId())) {
                     vc = VarContext.INDEX;
                 }
                 else {
                     vc = VarContext.INDEXED;
                 }
             } else if (v.getArity() == 1) {
                 vc = VarContext.SCALAR;
             } else if (v.getArity() > 1) {
                 vc = VarContext.LIST;
             }
             
         }
         return vc;
     }
 
     @Override
     public void setVarContext(VarContext t) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public VarType getVarType() {
          if (vt == null) {
              Variable v = model();
              if (v.getDataType() == DataType.NUM) {
                  vt= VarType.RANGE;
              } else if (v.getDataType() == DataType.TXT) {
                 vt= VarType.FREE;
              } else if (v.getDataType() == DataType.CAT) {
                 vt= VarType.CATEGORICAL;
              } else {
                  vt=VarType.FREE;
              }
          }
         return vt;
     }
 
     @Override
     public void setVarType(VarType t) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void setExternalInfo(String info) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public String getExternalInfo() {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean getIndex() {
         //return model.getVarContext() != null ? model.getVarContext().equals("INDEX") : false;
     	return isIndex;
     }
 
     @Override
     public void setIndex(boolean b) {
         isIndex = b;
         
     }
     @Override
     public void setCategories(String[] categories) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     private Class mapClass(DataType type, int precision) {
         if (type == DataType.NUM) {
             if (precision > 0) {
                 return Double.class;
             } else {
                 return Integer.class;
             }
         } else return String.class;
     }
 }
