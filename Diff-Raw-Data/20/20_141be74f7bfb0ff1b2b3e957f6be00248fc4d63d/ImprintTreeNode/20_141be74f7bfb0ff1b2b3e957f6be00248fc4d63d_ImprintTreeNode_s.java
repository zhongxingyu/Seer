 package uk.ac.ebi.age.admin.client.ui;
 
 import uk.ac.ebi.age.admin.client.model.AgeAbstractClassImprint;
 
 import com.smartgwt.client.widgets.tree.TreeNode;
 
 public abstract class ImprintTreeNode extends TreeNode
 {
  private AgeAbstractClassImprint           cls;
 
  public ImprintTreeNode( AgeAbstractClassImprint cl )
  {
   cls=cl;
   
   setTitle(cl.getName());
   setIcon(getIcon());
   setAttribute("Name", cl.getName());
  }
  
  public abstract String getIcon(); 
 
  public AgeAbstractClassImprint getClassImprint()
  {
   return cls;
  }
 
  public void setClassImprint(AgeAbstractClassImprint cls)
  {
   this.cls = cls;
  }
 
  public void updateType()
  {
   setIcon(getIcon());
  }
 
 
 }
