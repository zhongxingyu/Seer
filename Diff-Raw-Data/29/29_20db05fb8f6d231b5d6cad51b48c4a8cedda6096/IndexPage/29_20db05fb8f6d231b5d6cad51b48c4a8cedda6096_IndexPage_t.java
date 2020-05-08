 package ru.skalodrom_rf.web.pages;
 
 import net.sf.wicketautodao.model.HibernateModel;
 import net.sf.wicketautodao.model.HibernateModelList;
 import net.sf.wicketautodao.model.HibernateQueryDataProvider;
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.PageParameters;
 import org.apache.wicket.RequestCycle;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.form.AjaxButton;
 import org.apache.wicket.datetime.markup.html.form.DateTextField;
 import org.apache.wicket.extensions.yui.calendar.DatePicker;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.ChoiceRenderer;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.markup.repeater.data.DataView;
 import org.apache.wicket.markup.repeater.data.IDataProvider;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.protocol.http.WebRequest;
 import org.apache.wicket.protocol.http.WebResponse;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.joda.time.LocalDate;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import ru.skalodrom_rf.dao.PrefferedWeekDayDao;
 import ru.skalodrom_rf.dao.ProfileDao;
 import ru.skalodrom_rf.dao.SkalodromDao;
 import ru.skalodrom_rf.model.ClimbLevel;
 import ru.skalodrom_rf.model.Profile;
 import ru.skalodrom_rf.model.Skalodrom;
 import ru.skalodrom_rf.model.Time;
 import ru.skalodrom_rf.web.EnumRendererer;
 
 import javax.servlet.http.Cookie;
import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.util.Arrays;
 import java.util.Date;
 
 /**
  */
 public class IndexPage extends BasePage{
     private static final Logger LOG= LoggerFactory.getLogger(IndexPage.class);
     @SpringBean
     ProfileDao profileDao;
     @SpringBean
     private SkalodromDao skalodromDao;
     @SpringBean
     PrefferedWeekDayDao prefferedWeekDayDao;
 
     private final HibernateModel<Skalodrom> skalModel = new HibernateModel<Skalodrom>(getDefaultSkalodrom());
 
     private final Model<Date> dateModel = new Model<Date>(getDefaultDate());
     private final Model<LocalDate> localDateModel = new Model<LocalDate>(LocalDate.fromDateFields(getDefaultDate()));
 
     WebMarkupContainer resultContainer=new WebMarkupContainer("wrapper");//container for ajax update of result table
     WebMarkupContainer resultContainer2=new WebMarkupContainer("wrapper2");  //container for ajax update of result table
     public IndexPage() {
         this(new PageParameters());
     }
 
     public IndexPage(PageParameters parameters) {
         super(parameters);
 
         final Form form = new Form("form");
 
         add(form);
 
         ChoiceRenderer<Skalodrom> scalodromRenderer = new ChoiceRenderer<Skalodrom>("name", "name");
 
         final HibernateModelList<Skalodrom> modelList = new HibernateModelList<Skalodrom>(skalodromDao.findAll());
         final DropDownChoice dropDownChoice = new DropDownChoice<Skalodrom>("scalodrom", modelList, scalodromRenderer);
 
         dropDownChoice.setModel(skalModel);
         form.add(dropDownChoice);
 
         DateTextField dateTextField = DateTextField.forShortStyle("date", dateModel);
         DatePicker datePicker = new DatePicker();
         datePicker.setShowOnFieldClick(true);
         dateTextField.add(datePicker);
         form.add(dateTextField);
 
         final Model<Time> timeModel =  new Model<Time>(getDefaultTime());
 
         final EnumRendererer<Time> timeRenderer = new EnumRendererer<Time>(Time.class);
         form.add(new  DropDownChoice<Time>("time",timeModel, Arrays.asList(Time.values()), timeRenderer));
 
         form.add(new AjaxButton("submit"){
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 LOG.debug("skalModel = {}" , skalModel.getObject().getName());
                 LOG.debug("dateModel = {}" , dateModel.getObject());
                 LOG.debug("timeModel = {}" , localDateModel.getObject());
                 final LocalDate localDate = LocalDate.fromDateFields(dateModel.getObject());
                 localDateModel.setObject(localDate);
                 target.addComponent(resultContainer);
                 target.addComponent(resultContainer2);
                 WebResponse response = (WebResponse) getRequestCycle().getResponse();
                String encodedSkalodrom = null;
                try {
                    encodedSkalodrom = URLEncoder.encode(skalModel.getObject().getName(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    LOG.error("no UTF-8 encoding!!!",e);
                }
                response.addCookie(new Cookie("defaultSkalodrom", encodedSkalodrom));
                 response.addCookie(new Cookie("defaultTime", ""+timeModel.getObject().name()));
                 response.addCookie(new Cookie("defaultDate", ""+dateModel.getObject().getTime()));
 
             }
         });
 
         resultContainer.setOutputMarkupId(true);
         final HibernateQueryDataProvider dataProvider = new HibernateQueryDataProvider(ProfileDao.class, "findByScalodromAndDate",skalModel,localDateModel,timeModel);
         resultContainer.add(createResults("profilesTable",dataProvider));
         add(resultContainer);
 
         final WeekDayModelWrapper weekModel = new WeekDayModelWrapper(localDateModel);
         final HibernateQueryDataProvider dataProvider2 = new HibernateQueryDataProvider(ProfileDao.class, "findByScalodromAndWeekDay",skalModel, weekModel);
         resultContainer2.setOutputMarkupId(true);
         resultContainer2.add(createResults("profilesTable2",dataProvider2));
         add(resultContainer2);
 
 
     }
 
     private Date getDefaultDate() {
          WebRequest request = (WebRequest) getRequestCycle().getRequest();
         Cookie defaultDate = request.getCookie("defaultDate");
         Date now = new Date();
         try {
             Date date = new Date(Long.parseLong(defaultDate.getValue()));
             if(date.after(now)){
                 return date;
             }else{
                 return now;
             }
 
         } catch (Exception e) {
             return now;
         }
 
     }
 
  private Time getDefaultTime() {
          WebRequest request = (WebRequest) getRequestCycle().getRequest();
         Cookie defaultTime = request.getCookie("defaultTime");
         try {
             Time time = Time.valueOf(defaultTime.getValue());
             return time;
         } catch (Exception e) {
             return Time.EVENING;
         }
 
     }
 
     private Skalodrom getDefaultSkalodrom() {
         try {
             WebRequest request = (WebRequest) getRequestCycle().getRequest();
             Cookie defaultSkalodrom = request.getCookie("defaultSkalodrom");
            String value = URLDecoder.decode(defaultSkalodrom.getValue(),"UTF-8");
             Skalodrom skalodrom = skalodromDao.findByName(value);
             if(skalodrom==null){
                   return skalodromDao.findAll().get(0);
             } else{
                 return skalodrom ;
             }
         } catch (Exception e) {
             return skalodromDao.findAll().get(0);
         }
     }
 
     private DataView createResults(String id, IDataProvider<Profile> dataProvider) {
         return new DataView<Profile>(id,dataProvider){
             final EnumRendererer climbLevelRenderer = new EnumRendererer<ClimbLevel>(ClimbLevel.class);
             @Override
             protected void populateItem(final Item<Profile> hibernateModelItem) {
 
                 if (hibernateModelItem.getIndex()%2==0){
                     hibernateModelItem.add(new AttributeModifier("class", true, new Model<String>("sec")));
                 }
                 final Profile profile = hibernateModelItem.getModelObject();
                 final HibernateModel model = new HibernateModel(profile);
                 final PageParameters pageParameters = new PageParameters("0="+profile.getUser().getLogin());
 
 
                 BookmarkablePageLink a1 = new BookmarkablePageLink("viewProfile1", ProfileViewPage.class, pageParameters);;
                 BookmarkablePageLink a2 = new BookmarkablePageLink("viewProfile2", ProfileViewPage.class, pageParameters);
                 hibernateModelItem.add(a1);
                 hibernateModelItem.add(a2);
                a1.add(new Label("fio", profile.getFio()));
 
 
 
                 hibernateModelItem.add(new Label("weight",profile.getWeight()==null?"":""+profile.getWeight()));
 
                 hibernateModelItem.add(new Label("level", climbLevelRenderer.getDisplayValue(profile.getClimbLevel())));
 
 
                 hibernateModelItem.add(new Link("sendMessage"){
                     @Override
                     public void onClick() {
                         RequestCycle.get().setResponsePage(new SendMessagePage(model));
                     }
                 });
             }
 
         };
     }
 }
