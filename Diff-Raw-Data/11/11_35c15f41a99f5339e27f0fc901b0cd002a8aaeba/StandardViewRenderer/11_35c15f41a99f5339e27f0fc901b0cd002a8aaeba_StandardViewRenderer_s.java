 package com.redshape.renderer.json.renderers;
 
 import com.redshape.renderer.IRenderersFactory;
 import com.redshape.servlet.views.IView;
 import com.redshape.utils.Commons;
 
 import java.util.Map;
 
 /**
  * @author Cyril A. Karpenko <self@nikelin.ru>
  * @package com.redshape.renderer.json.renderers
  * @date 3/1/12 {3:32 PM}
  */
 public class StandardViewRenderer extends AbstractJSONRenderer<IView> {
 
     private IRenderersFactory renderderesFactory;
 
     public StandardViewRenderer( IRenderersFactory renderersFactory ) {
         this.renderderesFactory = renderersFactory;
     }
 
     @Override
     public void repaint(IView renderable) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     protected String[] renderAttributes( Map<String, Object> attributes ) {
         String[] result = new String[attributes.size()];
         
         int i = 0 ;
         for ( Map.Entry<String, Object> attribute : attributes.entrySet() ) {
             result[i++] =
                 this.createField(attribute.getKey(),
                     attribute.getValue() != null ?
                        Commons.select(this.getRenderderesFactory()
                                .<Object, String>forEntity(attribute.getValue())
                                .render(attribute.getValue()),
                        "")
                         : "null"
                 );
         }
 
         return result;
     }
     
     @Override
     public String render(IView renderable) {
         StringBuilder builder = new StringBuilder();
         builder.append(
            this.createObject(
                this.createField("view",
                    this.createObject(
                        this.createField("attributes",
                            this.createObject( this.renderAttributes(renderable.getAttributes()) ) )
                    )
                )
            )
         );
         
         return builder.toString();
     }
 
     protected IRenderersFactory getRenderderesFactory() {
         return renderderesFactory;
     }
 }
