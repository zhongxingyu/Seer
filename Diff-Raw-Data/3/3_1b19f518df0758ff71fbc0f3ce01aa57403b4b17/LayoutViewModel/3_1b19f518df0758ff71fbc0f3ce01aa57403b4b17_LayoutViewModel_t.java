 package com.page5of4.mustache;
 
 public class LayoutViewModel {
 
    private final ApplicationModel applicationModel;
    private final LayoutBodyFunction layoutBodyFunction;
    private final Object bodyModel;
    private final Object layoutModel;
 
    public ApplicationModel getApplicationModel() {
       return applicationModel;
    }
 
    public String getBody() {
      if(layoutBodyFunction == null) {
         throw new RuntimeException("{{body}} is only valid from within a Layout.");
      }
       return layoutBodyFunction.getBody();
    }
 
    public Object getBodyModel() {
       return bodyModel;
    }
 
    public Object getLayoutModel() {
       return layoutModel;
    }
 
    public LayoutViewModel(ApplicationModel applicationModel, Object bodyModel, Object layoutModel, LayoutBodyFunction layoutBodyFunction) {
       super();
       this.applicationModel = applicationModel;
       this.bodyModel = bodyModel;
       this.layoutModel = layoutModel;
       this.layoutBodyFunction = layoutBodyFunction;
    }
 
 }
