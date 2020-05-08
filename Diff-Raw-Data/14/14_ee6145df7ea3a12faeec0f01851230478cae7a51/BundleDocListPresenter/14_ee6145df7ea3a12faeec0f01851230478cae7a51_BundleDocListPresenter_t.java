 package kkckkc.jsourcepad.bundleeditor;
 
 import kkckkc.jsourcepad.bundleeditor.command.CommandBundleDocPresenter;
 import kkckkc.jsourcepad.bundleeditor.command.CommandBundleDocViewImpl;
 import kkckkc.jsourcepad.bundleeditor.manifest.ManifestBundleDocPresenter;
 import kkckkc.jsourcepad.bundleeditor.manifest.ManifestBundleDocViewImpl;
 import kkckkc.jsourcepad.bundleeditor.model.BundleDocImpl;
 import kkckkc.jsourcepad.bundleeditor.template.TemplateBundleDocPresenter;
 import kkckkc.jsourcepad.bundleeditor.template.TemplateBundleDocViewImpl;
 import kkckkc.jsourcepad.model.Doc;
 import kkckkc.jsourcepad.model.bundle.BundleStructure;
 import kkckkc.jsourcepad.ui.DocListPresenter;
 import kkckkc.jsourcepad.ui.DocPresenter;
 
 public class BundleDocListPresenter extends DocListPresenter {
 
     @Override
     protected DocPresenter createPresenter(Doc doc) {
         if (! (doc instanceof BundleDocImpl)) return super.createPresenter(doc);
 
         BundleDocImpl bdi = (BundleDocImpl) doc;
 
         if (bdi.getType() == null) {
             return super.createPresenter(doc);
         }
 
         if (bdi.getType() == BundleStructure.Type.COMMAND) {
 
             CommandBundleDocPresenter presenter = new CommandBundleDocPresenter();
             presenter.setDoc(doc);
            presenter.setWindow(docList.getWindow());
             presenter.setView(new CommandBundleDocViewImpl());
             presenter.init();
 
             return presenter;
 
         } else if (bdi.getType() == BundleStructure.Type.TEMPLATE) {
 
            TemplateBundleDocPresenter presenter = new TemplateBundleDocPresenter();
            presenter.setDoc(doc);
           presenter.setWindow(docList.getWindow());
            presenter.setView(new TemplateBundleDocViewImpl());
            presenter.init();
 
            return presenter;
 
         } else if (bdi.getType() == BundleStructure.Type.MANIFEST) {
 
             ManifestBundleDocPresenter presenter = new ManifestBundleDocPresenter();
             presenter.setDoc(doc);
            presenter.setWindow(docList.getWindow());
             presenter.setView(new ManifestBundleDocViewImpl());
             presenter.init();
 
             return presenter;
 
 
         } else {
 
             BasicBundleDocPresenter presenter = new BasicBundleDocPresenter();
             presenter.setDoc(doc);
            presenter.setWindow(docList.getWindow());
             presenter.setView(new BasicBundleDocViewImpl());
             presenter.init();
 
             return presenter;
         }
     }
 }
