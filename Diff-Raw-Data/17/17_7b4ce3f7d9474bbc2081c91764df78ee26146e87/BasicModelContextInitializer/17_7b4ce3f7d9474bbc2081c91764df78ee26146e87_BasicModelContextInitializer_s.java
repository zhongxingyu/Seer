 package com.polopoly.ps.dispatchlightly.initializer;
 
 import static com.polopoly.cm.servlet.RequestPreparator.getURLBuilder;
 
 import com.polopoly.cm.client.CMRuntimeException;
 import com.polopoly.cm.servlet.URLBuilder;
 import com.polopoly.ps.dispatchlightly.ModelContext;
 import com.polopoly.ps.dispatchlightly.ModelContextInitializer;
 import com.polopoly.ps.dispatchlightly.model.ContentPathUtilWrapper;
 import com.polopoly.ps.dispatchlightly.model.LightURLBuilderWrapper;
 import com.polopoly.ps.dispatchlightly.model.PreviewMode;
 import com.polopoly.ps.dispatchlightly.polopoly.RenderMode;
 import com.polopoly.ps.layout.element.util.ControllerUtil;
 import com.polopoly.ps.layout.element.util.NoCurrentPageException;
 import com.polopoly.ps.layout.element.util.NoPageScopeAvailableException;
 import com.polopoly.siteengine.model.request.PreviewScope;
 import com.polopoly.siteengine.resource.Resources;
 import com.polopoly.siteengine.structure.Page;
 import com.polopoly.siteengine.structure.Site;
 
 /**
  * TODO Break up into a class per variable.
  */
 public class BasicModelContextInitializer implements ModelContextInitializer {
 
 	@Override
 	public void initialize(ModelContext context, ControllerUtil util) {
 		context.put(context);
 		context.put(new ContentPathUtilWrapper(util));
 		context.put(util.getPolopolyContext());
 		context.put(util.getPolicyCMServer());
 		context.put(new RenderMode(util.getControllerContext().getMode()));
 		context.put(util.getPolicy());
 
 		try {
 			context.put(util.getPage(Page.class));
 		} catch (NoCurrentPageException e1) {
 			// fine.
 		}
 
 		try {
 			context.put(util.getSite(Site.class));
 		} catch (CMRuntimeException e1) {
 			// fine.
 		}
 
 		context.put(util.getRequest());
 		context.put(util);
		context.put(util.getModel().getContext().getPage());
 
 		try {
 			context.put(util.getContentPath());
 		} catch (NoPageScopeAvailableException e) {
 			// ignore.
 		}
 
		context.put(calculatePreviewModel(util.getModel().getRequest()
				.getPreview()));
 
 		try {
 			Resources resources = util.getSite(Site.class).getResources();
 			context.put(resources);
 			context.put(resources.getLocale());
 		} catch (CMRuntimeException e) {
 			// no site. ignore.
 		}
 
 		URLBuilder urlBuilder = getURLBuilder(util.getRequest());
 
 		if (urlBuilder != null) {
			context.put(new LightURLBuilderWrapper(urlBuilder, util
					.getRequest()));
 		}
 	}
 
 	private PreviewMode calculatePreviewModel(PreviewScope preview) {
 		if (preview == null) {
 			return PreviewMode.NON_PREVIEW;
 		} else if (preview.isInInteractivePreviewMode()) {
 			return PreviewMode.INTERACTIVE_PREVIEW;
 		} else if (preview.isInPreviewMode()) {
 			return PreviewMode.PREVIEW;
 		} else {
 			return PreviewMode.NON_PREVIEW;
 		}
 	}
 }
