 package org.xmodel.listeners;
 
 import java.util.List;
 import org.xmodel.IModelObject;
 import org.xmodel.xpath.expression.ExpressionListener;
 import org.xmodel.xpath.expression.IContext;
 import org.xmodel.xpath.expression.IExpression;
 
 /**
  * An abstract implementation of IExpressionListener that notifies subclasses of changes to the
  * Object value of the expression.  If the expression returns a node-set, then notifications
  * will be provided when the value of the first node in the node-set changes.  Otherwise,
  * notifications are provided when the value of the expression changes.
  * <p>
  * An instance of the listener may only be installed on one expression at a time.
  */
 public abstract class NodeValueListener extends ExpressionListener
 {
   /**
    * Called when the Object value of the expression changes.
    * @param context The context.
    * @param newValue The new value.
    * @param oldValue The old value.
    */
   protected abstract void notifyValue( IContext context, Object newValue, Object oldValue);
   
   /* (non-Javadoc)
    * @see org.xmodel.xpath.expression.ExpressionListener#notifyAdd(org.xmodel.xpath.expression.IExpression, 
    * org.xmodel.xpath.expression.IContext, java.util.List)
    */
   @Override
   public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
   {
     if ( owner == null) owner = expression;
     if ( owner != expression) throw new IllegalStateException( "NodeValueListener added to more than one expression!");
     
     nodes = expression.query( context, null);
     if ( node != nodes.get( 0))
     {
       Object oldValue = (node != null)? node.getValue(): null;
       node = nodes.get( 0);
       Object newValue = (node != null)? node.getValue(): null;
       notifyValue( context, newValue, oldValue);
     }
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.expression.ExpressionListener#notifyRemove(org.xmodel.xpath.expression.IExpression, 
    * org.xmodel.xpath.expression.IContext, java.util.List)
    */
   @Override
   public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
   {
     if ( owner == null) owner = expression;
     if ( owner != expression) throw new IllegalStateException( "NodeValueListener added to more than one expression!");
     
     if ( node != null && nodes.contains( node))
     {
       nodes = expression.query( context, null);
       Object oldValue = (node != null)? node.getValue(): null;
       node = (nodes.size() > 0)? nodes.get( 0): null;
       Object newValue = (node != null)? node.getValue(): null;
       notifyValue( context, newValue, oldValue);
     }
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.expression.ExpressionListener#notifyChange(org.xmodel.xpath.expression.IExpression, 
    * org.xmodel.xpath.expression.IContext, boolean)
    */
   @Override
   public void notifyChange( IExpression expression, IContext context, boolean newValue)
   {
     notifyValue( context, newValue, !newValue);
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.expression.ExpressionListener#notifyChange(org.xmodel.xpath.expression.IExpression, 
    * org.xmodel.xpath.expression.IContext, double, double)
    */
   @Override
   public void notifyChange( IExpression expression, IContext context, double newValue, double oldValue)
   {
     notifyValue( context, newValue, oldValue);
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.expression.ExpressionListener#notifyChange(org.xmodel.xpath.expression.IExpression, 
    * org.xmodel.xpath.expression.IContext, java.lang.String, java.lang.String)
    */
   @Override
   public void notifyChange( IExpression expression, IContext context, String newValue, String oldValue)
   {
     notifyValue( context, newValue, oldValue);
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.expression.ExpressionListener#notifyValue(org.xmodel.xpath.expression.IExpression, 
    * org.xmodel.xpath.expression.IContext[], org.xmodel.IModelObject, java.lang.Object, java.lang.Object)
    */
   @Override
   public void notifyValue( IExpression expression, IContext[] contexts, IModelObject object, Object newValue, Object oldValue)
   {
    if ( node == object || node.equals( object)) notifyValue( contexts[ 0], newValue, oldValue);
   }
   
   /* (non-Javadoc)
    * @see org.xmodel.xpath.expression.ExpressionListener#requiresValueNotification()
    */
   @Override
   public boolean requiresValueNotification()
   {
     return true;
   }
 
   private IExpression owner;
   private IModelObject node;
 }
