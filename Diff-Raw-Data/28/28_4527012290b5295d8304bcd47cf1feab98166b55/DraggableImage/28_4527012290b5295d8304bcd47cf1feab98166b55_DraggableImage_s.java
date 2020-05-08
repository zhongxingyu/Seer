 package wicket.contrib.scriptaculous.dragdrop;
 
 import wicket.contrib.scriptaculous.ScriptaculousAjaxHandler;
 import wicket.markup.ComponentTag;
 import wicket.markup.html.image.Image;
 
 public class DraggableImage extends Image {
 
     private final String id;
 
     public DraggableImage(String wicketId, String id, String img) {
         super(wicketId, img);
         this.id = id;
 
 		add(ScriptaculousAjaxHandler.newJavascriptBindingHandler());
     }
 
    protected void onRender() {
        super.onRender();
 
         getResponse().write("\n<script type=\"text/javascript\">new Draggable('" + id + "', {revert:true})</script>");
     }
 
     protected void onComponentTag(ComponentTag tag) {
         super.onComponentTag(tag);
         tag.put("id", id);
         tag.put("class", getId());
     }
 }
