 package wicket.contrib.scriptaculous.dragdrop;
 
 import wicket.PageParameters;
 import wicket.contrib.scriptaculous.Indicator;
 import wicket.contrib.scriptaculous.ScriptaculousAjaxHandler;
 import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
 import wicket.markup.html.WebMarkupContainer;
 
 public class DraggableTarget extends WebMarkupContainer {
 
     private final Class pageContribution;
     private String draggableClass;
 	private String indicatorId;
 
     public DraggableTarget(String id, Class pageContribution) {
         super(id);
         this.pageContribution = pageContribution;
 
 		add(ScriptaculousAjaxHandler.newJavascriptBindingHandler());
     }
 
     public void accepts(DraggableImage image) {
     	this.draggableClass = image.getId();
     }
 
     public void setIndicator(Indicator indicator) {
     	this.indicatorId = indicator.getId();
     }
 
    protected void onRender(MarkupStream markupStream) {
        super.onRender(markupStream);
 
         String url = this.getPage().urlFor(null, pageContribution, new PageParameters());
         getResponse().write("\n<script type=\"text/javascript\">new Ajax.Updater('" + getId() + "', '"+ url + "', " +
                 " { " +
                 " evalScripts:true, " +
                 " asynchronous:true" +
                 " })" +
         "</script>\n");
         getResponse().write("<script type=\"text/javascript\">Droppables.add('"+ getId() + "', " +
                 " {accept:'" + draggableClass + "', " +
                 " onDrop:function(element){ " +
                     " new Ajax.Updater('" + getId() + "', '"+ url + "', " +
                         " { ");
         if (null != indicatorId) {
         	getResponse().write(" onLoading:function(request){ Element.show('indicator')}, " +
                   " onComplete:function(request){Element.hide('indicator')}, ");
         }
         getResponse().write(" parameters:'id=' + encodeURIComponent(element.id), " +
                         " evalScripts:true, " +
                         " asynchronous:true" +
                         " }" +
                 ")}, " +
                 " hoverclass:'" + getId() +"-active'})</script>\n");
     }
 
     protected void onComponentTag(ComponentTag tag) {
         super.onComponentTag(tag);
         tag.put("id", getId());
     }
 }
