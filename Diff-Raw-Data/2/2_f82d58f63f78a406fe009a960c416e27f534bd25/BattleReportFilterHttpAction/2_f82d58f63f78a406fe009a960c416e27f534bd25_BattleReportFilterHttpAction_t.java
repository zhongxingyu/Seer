 package polly.rx.http;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import de.skuzzle.polly.sdk.Configuration;
 import de.skuzzle.polly.sdk.MyPolly;
 import de.skuzzle.polly.sdk.exceptions.DatabaseException;
 import de.skuzzle.polly.sdk.exceptions.InsufficientRightsException;
 import de.skuzzle.polly.sdk.http.HttpAction;
 import de.skuzzle.polly.sdk.http.HttpEvent;
 import de.skuzzle.polly.sdk.http.HttpSession;
 import de.skuzzle.polly.sdk.http.HttpTemplateContext;
 import de.skuzzle.polly.sdk.http.HttpTemplateException;
 import de.skuzzle.polly.sdk.http.HttpTemplateSortHelper;
 import polly.rx.core.FleetDBManager;
 import polly.rx.http.session.BattleReportFilterSettings;
 import polly.rx.core.filter.BattleReportFilter;
 import polly.rx.core.filter.AnyDayFilter;
 import polly.rx.core.filter.AttackerFilter;
 import polly.rx.core.filter.DefenderFilter;
 import polly.rx.core.filter.AttackerClanFilter;
 import polly.rx.core.filter.DefenderClanFilter;
 import polly.rx.core.filter.LocationFilter;
 import polly.rx.core.filter.HasArtifactFilter;
 import polly.rx.core.filter.TacticFilter;
 import polly.rx.core.filter.IdListFilter;
 
 import polly.rx.entities.BattleReport;
 import polly.rx.entities.BattleTactic;
 
 
 
 public class BattleReportFilterHttpAction extends HttpAction {
     
     
     private String dateFormat;
     private FleetDBManager fleetDBManager;
     
     
 
     public BattleReportFilterHttpAction(MyPolly myPolly, FleetDBManager fleetDBManager) {
         super("/filter_reports", myPolly);
         this.requirePermission(FleetDBManager.VIEW_BATTLE_REPORT_PERMISSION);
         this.fleetDBManager = fleetDBManager;
         
         
         Configuration cfg = myPolly.configuration().getRootConfiguration();
         this.dateFormat = cfg.readString(Configuration.DATE_FORMAT);
     }
 
     
     
     @Override
     public HttpTemplateContext execute(HttpEvent e) throws HttpTemplateException, 
             InsufficientRightsException {
         
         BattleReportFilterSettings settings = 
                 (BattleReportFilterSettings) e.getSession().get(
                     TemplateContextHelper.FILTER_SETTINGS);
         
         if (settings == null) {
             settings = new BattleReportFilterSettings();
             e.getSession().putDtata(TemplateContextHelper.FILTER_SETTINGS, settings);
         }
         
         String action = e.getProperty("action");
         boolean isDelete = e.getProperty("delete") != null;
         boolean isSelect = e.getProperty("select") != null;
         
         if (action != null && action.equals("idSelect") && isSelect) {
             Integer[] ids = this.getIdList(e);
             settings.addFilter(BattleReportFilterSettings.ID_LIST_FILTER, 
                 new IdListFilter(ids));
             
         } else if (action != null && action.equals("idSelect") && isDelete) {
             
             if (!this.getMyPolly().roles().hasPermission(e.getSession().getUser(), 
                     FleetDBManager.DELETE_BATTLE_REPORT_PERMISSION)) {
                 throw new InsufficientRightsException(this);
             }
             
             Integer[] ids = this.getIdList(e);
             
             try {
                 this.fleetDBManager.deleteReportByIdList(ids);
             } catch (DatabaseException e1) {
                 e.throwTemplateException(e1);
             }
             
         } else if (action != null && action.equals("toggleSwitching")) {
             settings.setSwitchOnAlienAttack(!settings.isSwitchOnAlienAttack());
         } else if (action != null && action.equals("or")) {
             settings.filterOr();
         } else if (action != null && action.equals("and")) {
             settings.filterAnd();
         } else if (action != null && action.equals("clear")) {
             settings.clearAll();
         } else if (action != null && action.equals("removeFilter")) {
             String filterKey = e.getProperty("filterKey");
             settings.removeFilter(filterKey);
             
         } else if (action != null && action.equals("addFilter")) {
             String filterKey = e.getProperty("filterKey");
             String filterParam = e.getProperty("param");
             
             if (filterKey != null && 
                 filterKey.equals(BattleReportFilterSettings.DATE_FILTER)) {
                 
                 BattleReportFilter filter = this.createAnyDayFilter(
                     filterParam, e.getSession());
                 settings.addFilter(filterKey, filter);
             } else if (filterKey != null &&
                 filterKey.equals(BattleReportFilterSettings.ATTACKER_FILTER)) {
                 
                 settings.addFilter(filterKey, new AttackerFilter(filterParam));
             } else if (filterKey != null &&
                 filterKey.equals(BattleReportFilterSettings.DEFENDER_FILTER)) {
                 
                 settings.addFilter(filterKey, new DefenderFilter(filterParam));
             } else if (filterKey != null &&
                 filterKey.equals(BattleReportFilterSettings.DEFENDER_CLAN_FILTER)) {
                 
                 settings.addFilter(filterKey, new DefenderClanFilter(filterParam));
             } else if (filterKey != null &&
                 filterKey.equals(BattleReportFilterSettings.ATTACKER_CLAN_FILTER)) {
                 
                 settings.addFilter(filterKey, new AttackerClanFilter(filterParam));
             } else if (filterKey != null &&
                 filterKey.equals(BattleReportFilterSettings.LOCATION_FILTER)) {
                 
                 settings.addFilter(filterKey, new LocationFilter(filterParam));
             } else if (filterKey != null &&
                 filterKey.equals(BattleReportFilterSettings.HAS_ARTIFACT_FILTER)) {
                 
                 settings.addFilter(filterKey, new HasArtifactFilter());
             } else if (filterKey != null &&
                 filterKey.equals(BattleReportFilterSettings.TACTIC_FILTER)) {
                 
                 BattleTactic tactic = BattleTactic.parseTactic(filterParam);
                 settings.addFilter(filterKey, new TacticFilter(tactic));
             } else {
                 e.throwTemplateException("Invalid filter options", "");
             }
         }
         
         
         List<BattleReport> reports = this.fleetDBManager.getAllReports();
         HttpTemplateContext c = new HttpTemplateContext("pages/query_reports.html");
         TemplateContextHelper.prepareForReportsList(c, e.getSession(), reports);
         HttpTemplateSortHelper.makeListSortable(c, e, "sortKey", "dir", "getDate");
         
         return c;
     }
     
     
     
     private BattleReportFilter createAnyDayFilter(String param, HttpSession session) 
             throws HttpTemplateException {
         
         DateFormat df = new SimpleDateFormat(this.dateFormat);
         Date base;
         try {
             base = df.parse(param);
         } catch (ParseException e) {
             throw new HttpTemplateException(session, e);
         }
         return new AnyDayFilter(base);
     }
 
     
     
     private Integer[] getIdList(HttpEvent e) {
         String[] stringIds = e.getProperty("selectRP").split(";");
         Integer ids[] = new Integer[stringIds.length];
         
         for (int i = 0; i < stringIds.length; ++i) {
             ids[i] = Integer.parseInt(stringIds[i]);
         }
         return ids;
     }
 }
