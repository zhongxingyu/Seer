 package wicket.contrib.mootools.plugins;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.Response;
 
 import wicket.contrib.mootools.AbstractRequireMooStatelessBehavior;
 import wicket.contrib.mootools.MFXJavascriptUtils;
 import wicket.contrib.mootools.MFXOptions;
 
 public class MFXFontResizer extends AbstractRequireMooStatelessBehavior {
 	private static final long serialVersionUID = 1L;
 	private int size;
 	private Component target;
 	private MFXOptions mfxOptions = new MFXOptions();
 	private String container;
 	private String tag = "p";
 
 	public MFXFontResizer(final int size) {
 		this.size = size;
 	}
 
 	public MFXFontResizer(final int size, final int maxSize) {
 		this.size = size;
 	}
 
 	@Override
 	public void onRendered(final Component arg0) {
 		super.onRendered(arg0);
 		Response resp = arg0.getResponse();
 		resp.write(MFXJavascriptUtils.DOM_READY_OPEN());
 		arg0.setOutputMarkupId(true);
 		target = arg0;
 		resp.write(mooFunction());
 		resp.write(MFXJavascriptUtils.DOM_READY_CLOSE());
 	}
 
 	@Override
 	public String mooFunction() {
 		StringBuffer buf = new StringBuffer();
 		final String compid = target.getMarkupId();
 		buf.append("$('" + compid + "')" + ".addEvent('click',function(e) {");
 
 		if (container != null) {
 			buf.append("var texts = $ES('" + tag + "','" + container + "');");
 		} else {
 			buf.append("var texts = $$('" + tag + "');");
 		}
 
 		buf.append("texts.each(function(t,i) {");
 		buf.append("var csize = t.getStyle('font-size').toInt();");
 		buf.append("new Fx.Style(t,'font-size'," + mfxOptions.writeOptions() + ").start(csize, (csize+(" + size
 				+ ")));");
 		buf.append("});});");
 		return buf.toString();
 	}
 
 	public MFXFontResizer setContainer(final String container) {
 		this.container = container;
 		return this;
 	}
 
 	public String getContainer() {
 		return container;
 	}
 
 	public MFXFontResizer setTag(final String tag) {
 		this.tag = tag;
 		return this;
 	}
 
 	public String getTag() {
 		return tag;
 	}
 
 	public MFXFontResizer setMfxOptions(final MFXOptions mfxOptions) {
 		this.mfxOptions = mfxOptions;
 		return this;
 	}
 
 	public MFXOptions getMfxOptions() {
 		return mfxOptions;
 	}
 
 	// var larger = $('larger');
 	// var smaller = $('smaller');
 	// larger.addEvent('click', function(e) {
 	// var texts = $ES('p','stage');
 	// texts.each(function(t,i) {
 	// new Fx.Style(t,'font-size',{duration: 500}).start(12,16);
 	// });
 	// });
 	// smaller.addEvent('click', function(e) {
 	// var texts = $ES('p','stage');
 	// texts.each(function(t,i) {
 	// new Fx.Style(t,'font-size',{duration: 500}).start(16,12);
 	// });
 	// });
 }
