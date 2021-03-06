 //
 // $Id$
 
 package coreen.project;
 
 import com.google.common.base.Function;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.Hyperlink;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 
 import com.threerings.gwt.ui.Bindings;
 import com.threerings.gwt.ui.Popups;
 import com.threerings.gwt.ui.Widgets;
 import com.threerings.gwt.util.DateUtil;
 import com.threerings.gwt.util.Value;
 
 import coreen.client.AbstractPage;
 import coreen.client.Args;
 import coreen.client.ClientMessages;
 import coreen.client.Link;
 import coreen.client.Page;
 import coreen.model.Project;
 import coreen.rpc.ProjectService;
 import coreen.rpc.ProjectServiceAsync;
 import coreen.util.ClickCallback;
 import coreen.util.PanelCallback;
 
 /**
  * Displays a single project.
  */
 public class ProjectPage extends AbstractPage
 {
     /** Enumerates the different project detail pages. */
     public static enum Detail {
         /** Compilation units, by directory. */
         CUS(_msgs.pByDir()),
 
         /** Types, grouped alphabetically. */
         TPS(_msgs.pByTypes()),
 
         /** Viewing an individual type. */
         TYP(null),
 
         /** Viewing an individual source file. */
         SRC(null);
 
         public String title () {
             return _title;
         }
 
         Detail (String title) {
             _title = title;
         }
         protected String _title;
     }
 
     public ProjectPage ()
     {
         initWidget(_binder.createAndBindUi(this));
 
         // some UI elements are only visible/enabled when we have a project
         Value<Boolean> projp = _proj.map(new Function<Project,Boolean>() {
             public Boolean apply (Project proj) { return (proj != null); }
         });
         Bindings.bindEnabled(projp, _search, _go, _update);
         Bindings.bindVisible(projp, _header);
 
         new ClickCallback<Void>(_update) {
             protected boolean callService () {
                 _projsvc.updateProject(_proj.get().id, this);
                 return true;
             }
             protected boolean gotResult (Void result) {
                 Popups.infoNear(_msgs.pUpdateStarted(), _update);
                 return true;
             }
         };
 
         new ClickCallback<Void>(_go, _search) {
             protected boolean callService () {
                 return false; // TODO
             }
             protected boolean gotResult (Void result) {
                 return false; // TODO
             }
         };
     }
 
     @Override // from AbstractPage
     public Page getId ()
     {
         return Page.PROJECT;
     }
 
     @Override // from AbstractPage
     public void setArgs (final Args args)
     {
         final long projectId = args.get(0, 0L);
         final Detail detail = args.get(1, Detail.class, Detail.TPS);
         updateNavBar(projectId, detail);
 
         // if we have no project, or the wrong project, we must load the right project
         if (_proj.get() == null || _proj.get().id != projectId) {
             // clear out old project data
             _proj.update(null);
             _compunits = null;
 
             // load up the metadata for this project
             _contents.setWidget(Widgets.newLabel(_cmsgs.loading()));
             _projsvc.getProject(projectId, new PanelCallback<Project>(_contents) {
                 public void onSuccess (Project p) {
                     _proj.update(p);
                     _name.setText(p.name);
                     _name.setTargetHistoryToken(Args.createToken(Page.PROJECT, projectId));
                     _version.setText(p.version);
                     _imported.setText(DateUtil.formatDateTime(p.imported));
                     _lastUpdated.setText(DateUtil.formatDateTime(p.lastUpdated));
                     setArgs(args);
                 }
             });
             return;
         }
 
         switch (detail) {
         case CUS:
             if (_compunits == null) {
                 _compunits = new CompUnitsPanel(_proj.get());
             }
             _contents.setWidget(_compunits);
             break;
         case TPS: {
             TypesPanel panel;
             if (_contents.getWidget() instanceof TypesPanel) {
                 panel = (TypesPanel)_contents.getWidget();
             } else {
                 _contents.setWidget(panel = new TypesPanel());
             }
            panel.display(projectId, args.get(2, 0L));
            for (int idx = 3; args.get(idx, 0L) != 0L; idx++) {
                 panel.showMember(args.get(idx, 0L));
             }
             break;
         }
         case TYP: {
             long defId = args.get(2, 0L);
             TypeDetailPanel panel;
             if (_contents.getWidget() instanceof TypeDetailPanel &&
                 ((TypeDetailPanel)_contents.getWidget()).defId == defId) {
                 panel = (TypeDetailPanel)_contents.getWidget();
             } else {
                 _contents.setWidget(panel = new TypeDetailPanel(defId));
             }
             for (int idx = 3; args.get(idx, 0L) != 0L; idx++) {
                 panel.showMember(args.get(idx, 0L));
             }
             break;
         }
         case SRC:
             _contents.setWidget(new SourcePanel.Full(args.get(2, 0L), args.get(3, 0L)));
             break;
         }
     }
 
     protected void updateNavBar (long projectId, Detail current)
     {
         _navbar.clear();
         _navbar.add(Widgets.newInlineLabel("View: "));
         for (Detail detail : Detail.values()) {
             if (_navbar.getWidgetCount() > 1) {
                 _navbar.add(Widgets.newInlineLabel(" "));
             }
             if (detail == current) {
                 _navbar.add(Widgets.newInlineLabel(detail.title(), _styles.SelTitle()));
             } else {
                 _navbar.add(Link.createInline(detail.title(), Page.PROJECT, projectId, detail));
             }
         }
     }
 
     protected interface Styles extends CssResource
     {
         String SelTitle ();
     }
     protected @UiField Styles _styles;
 
     protected @UiField HTMLPanel _header;
     protected @UiField Hyperlink _name;
     protected @UiField Label _version, _imported, _lastUpdated;
     protected @UiField TextBox _search;
     protected @UiField Button _update, _go;
     protected @UiField FlowPanel _navbar;
     protected @UiField SimplePanel _contents;
 
     // keep some panels around for faster loading; eat that browser memory!
     protected CompUnitsPanel _compunits;
 
     protected Value<Project> _proj = Value.create(null);
 
     protected interface Binder extends UiBinder<Widget, ProjectPage> {}
     protected static final Binder _binder = GWT.create(Binder.class);
     protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
     protected static final ProjectMessages _msgs = GWT.create(ProjectMessages.class);
     protected static final ClientMessages _cmsgs = GWT.create(ClientMessages.class);
 
     // ensure that our shared CSS resources are injected into the DOM
     protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
     static {
         _rsrc.styles().ensureInjected();
     }
 }
