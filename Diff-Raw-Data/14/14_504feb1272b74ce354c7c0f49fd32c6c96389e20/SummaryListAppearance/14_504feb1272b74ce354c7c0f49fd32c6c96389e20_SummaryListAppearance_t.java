 package org.iplantc.de.client.sysmsgs.view;
 
 import org.iplantc.de.client.sysmsgs.view.DefaultMessagesViewResources.Style;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.sencha.gxt.widget.core.client.ListViewCustomAppearance;
 
 /**
  * The customized appearance for a list of system message summaries.
 * 
 * @param <M> the type of message
  */
public final class SummaryListAppearance<M> extends ListViewCustomAppearance<M> {
 
     private static final Style style;
 
     static {
         style = GWT.<DefaultMessagesViewResources> create(DefaultMessagesViewResources.class).style();
         style.ensureInjected();
     }
 
     /**
      * the constructor
      */
     public SummaryListAppearance() {
         super("." + style.summaryItem(), null, style.selected());
     }
 
     /**
      * @see ListViewCustomAppearance#renderItem(SafeHtmlBuilder, SafeHtml)
      */
     @Override
     public void renderItem(final SafeHtmlBuilder builder, final SafeHtml content) {
         builder.appendHtmlConstant("<div class='" + style.summaryItem() + "'>");
         builder.append(content);
         builder.appendHtmlConstant("</div>");
     }
 
 }
