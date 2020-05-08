 package org.jmaki;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.jmaki.model.IWidget;
 import org.jmaki.model.impl.GlobalConfig;
 import org.jmaki.model.impl.WebContext;
 import org.jmaki.model.impl.WidgetConfig;
 import org.jmaki.model.impl.WidgetFactory;
 import org.jmaki.model.impl.WidgetImpl;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import org.protorabbit.model.IParameter;
 import org.protorabbit.model.ITemplate;
 import org.protorabbit.model.impl.ResourceURI;
 import org.protorabbit.model.impl.BaseCommand;
 import org.protorabbit.model.impl.IncludeCommand;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class WidgetCommand extends BaseCommand {
 
    public static final String JMAKI_WRITTEN = "JMAKI_WRITTEN";
    private static final String WIDGETS_WRITTEN = null;
    private static Logger logger = null;
 
   public static final Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger("org.protrabbit");
        }
        return logger;
    }
 
    @SuppressWarnings("unchecked")
    @Override
    public void doProcess() throws IOException {
 
        if (params.length < 1 || params[0].getType() != IParameter.OBJECT) {
            getLogger().severe("Widget requires at least a name property");
            return;
        }
        JSONObject jo = (JSONObject)params[0].getValue();
        String name = null;
        String uuid = null;
        if (jo.has("name")) {
            try {
                name = jo.getString("name");
            } catch (JSONException e) {
              // do nothing
            }
        }
        if (jo.has("id")) {
            try {
                uuid = jo.getString("id");
            } catch (JSONException e) {
                // do nothing
            }
        }
        if (name == null){
            getLogger().severe("Widget requires at least a name property");
            return;
        }
 
        GlobalConfig gcfg = new GlobalConfig();
        // get request and response off the protorabbit context
        HttpServletRequest req = ((org.protorabbit.servlet.WebContext)this.ctx).getRequest();
        HttpServletResponse resp =  ((org.protorabbit.servlet.WebContext)this.ctx).getResponse();
        // create a web context
        WebContext wc = new WebContext( req, resp, gcfg);
        if (uuid == null) {
            uuid = wc.generateUuid(name.replace(".", "_"));
        }
        WidgetConfig wcfg = WidgetFactory.loadConfig(wc,name);
        IWidget widget = new WidgetImpl(name, uuid);
        // set generated attributes in widget config
        try {
            jo.put("widgetDir", wcfg.getWidgetDir());
            jo.put("uuid", uuid);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String tid = ctx.getTemplateId();
        ITemplate template = ctx.getConfig().getTemplate(tid);
        if (template != null) {
            Map<String, Boolean>widgetsWritten = (Map<String, Boolean>)template.getAttribute(WIDGETS_WRITTEN);
            if (widgetsWritten == null) {
                widgetsWritten = new HashMap<String, Boolean>();
                template.setAttribute(WIDGETS_WRITTEN, widgetsWritten);
            }
            // write the template
            buffer.write(WidgetFactory.getWidgetFragment(widget, wcfg, wc).toString().getBytes());
 
            // only write out the dependencies if they haven't been written
            if (widgetsWritten.get(name) == null) {
 
                Boolean jmakiWritten = (Boolean)template.getAttribute(JMAKI_WRITTEN);
 
                // copy in the dependencies
                List<org.protorabbit.model.impl.ResourceURI> scripts = template.getScripts();
                // check for jmaki.js
                if (jmakiWritten == null) {
                    List<org.protorabbit.model.impl.ResourceURI> ascripts = template.getAllScripts(ctx);
                    for (ResourceURI ri : ascripts) {
                        if (ri.getURI().endsWith("jmaki.js") ||
                            ri.getURI().endsWith("jmaki-min.js")) {
                            jmakiWritten = new Boolean(true);
                            template.setAttribute(JMAKI_WRITTEN, jmakiWritten);
                            break;
                        }
                    }
                }
 
                List<org.jmaki.model.impl.ResourceURI> wscripts = wcfg.getScripts();
 
                if (scripts == null) {
                    scripts = new ArrayList<org.protorabbit.model.impl.ResourceURI>();
                    template.setScripts(scripts);
                }
 
                if (jmakiWritten == null) {
                    scripts.add(new org.protorabbit.model.impl.ResourceURI("/resources/jmaki.js",
                                "",
                                org.protorabbit.model.impl.ResourceURI.SCRIPT));
                    template.setAttribute(JMAKI_WRITTEN, new Boolean(true));
                }
                scripts.add(new org.protorabbit.model.impl.ResourceURI(wcfg.getBaseDir() + "component.js",
                        "",
                        org.protorabbit.model.impl.ResourceURI.SCRIPT));
                if (wscripts != null) {
                    for (org.jmaki.model.impl.ResourceURI ri : wscripts) {
                        scripts.add(new org.protorabbit.model.impl.ResourceURI(ri.getURI(), ri.getBaseURI(), ri.getType()));
                    }
                }
                List<org.protorabbit.model.impl.ResourceURI> styles = template.getStyles();
                List<org.jmaki.model.impl.ResourceURI> wstyles = wcfg.getStyles();
 
                if (styles == null) {
                    styles = new ArrayList<org.protorabbit.model.impl.ResourceURI>();
                    template.setStyles(styles);
                }
                if (wcfg.getHasCss()) {
                    styles.add(new org.protorabbit.model.impl.ResourceURI( "component.css",
                            wcfg.getBaseDir() ,
                            org.protorabbit.model.impl.ResourceURI.LINK));
                }
                if (wstyles != null) {
                    for (org.jmaki.model.impl.ResourceURI ri : wstyles) {
                        styles.add(new org.protorabbit.model.impl.ResourceURI(ri.getURI(), ri.getBaseURI(), ri.getType()));
                    }
                }
                widgetsWritten.put(name, new Boolean(true));
            }
 
            // add deferred properties
            List<String> deferredScripts = (List<String>)ctx.getAttribute(IncludeCommand.DEFERRED_SCRIPTS);
            if (deferredScripts == null) {
                deferredScripts = new ArrayList<String>();
            }
            String widgetJavaScript = "<script>jmaki.addWidget(" + jo.toString() + ");</script>";
            deferredScripts.add(widgetJavaScript);
            ctx.setAttribute(IncludeCommand.DEFERRED_SCRIPTS, deferredScripts);
        }
    }
 }
