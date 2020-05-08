 package no.knubo.accounting.client.views;
 
 import no.knubo.accounting.client.Constants;
 import no.knubo.accounting.client.Elements;
 import no.knubo.accounting.client.I18NAccount;
 import no.knubo.accounting.client.Util;
 import no.knubo.accounting.client.misc.AuthResponder;
 import no.knubo.accounting.client.misc.ImageFactory;
 import no.knubo.accounting.client.misc.ServerResponse;
 import no.knubo.accounting.client.views.modules.AccountDetailLinesHelper;
 import no.knubo.accounting.client.views.modules.YearMonthComboHelper;
 
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONValue;
 import com.google.gwt.user.client.ui.ChangeListener;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.Widget;
 
 public class MonthDetailsView extends Composite implements ClickListener, ChangeListener,
         ServerResponse {
 
     private static MonthDetailsView me;
 
     private final Constants constants;
 
     private final I18NAccount messages;
 
     private Image backImage;
 
     private Image nextImage;
 
     private ListBox monthYearCombo;
 
     private YearMonthComboHelper yearMonthComboHelper;
 
     private int currentMonth;
 
     private int currentYear;
 
     private AccountDetailLinesHelper accountDetailLinesHelper;
 
     public MonthDetailsView(Constants constants, I18NAccount messages, Elements elements) {
         this.constants = constants;
         this.messages = messages;
 
         accountDetailLinesHelper = new AccountDetailLinesHelper(constants, messages, elements);
 
         DockPanel dp = new DockPanel();
 
         backImage = ImageFactory.previousImage("MonthDetailsView.backImage");
         backImage.addClickListener(this);
 
         nextImage = ImageFactory.nextImage("MonthDetailsView.nextImage");
         nextImage.addClickListener(this);
 
         monthYearCombo = new ListBox();
         monthYearCombo.setMultipleSelect(false);
         monthYearCombo.setVisibleItemCount(1);
         monthYearCombo.addChangeListener(this);
 
         yearMonthComboHelper = new YearMonthComboHelper(constants, monthYearCombo, elements);
 
         HorizontalPanel navPanel = new HorizontalPanel();
         navPanel.add(backImage);
         navPanel.add(monthYearCombo);
         navPanel.add(nextImage);
 
         dp.add(navPanel, DockPanel.NORTH);
         dp.add(accountDetailLinesHelper.getTable(), DockPanel.NORTH);
 
         initWidget(dp);
     }
 
     public static MonthDetailsView getInstance(Constants constants, I18NAccount messages,
             Elements elements) {
         if (me == null) {
             me = new MonthDetailsView(constants, messages, elements);
         }
         return me;
     }
 
     public void init() {
         yearMonthComboHelper.fillYearMonthCombo();
 
         accountDetailLinesHelper.init();
         currentMonth = 0;
         currentYear = 0;
 
         AuthResponder.get(constants, messages, me, "accounting/showmonthpost.php");
     }
 
     public void serverResponse(JSONValue value) {
         JSONArray array = value.isArray();
 
         accountDetailLinesHelper.renderResult(array, null);
         currentMonth = accountDetailLinesHelper.getMonthAfterRender();
         currentYear = accountDetailLinesHelper.getYearAfterRender();
 
         if (currentMonth > 0 && currentYear > 0) {
             yearMonthComboHelper.setIndex(currentYear, currentMonth);
         }
     }
 
     public void onClick(Widget sender) {
 
         if (sender == nextImage) {
             int m = currentMonth + 1;
 
             if (m > 12) {
                 int y = currentYear + 1;
                 load(y, m);
             } else {
                 load(currentYear, m);
             }
         } else {
             int m = currentMonth - 1;
 
             if (m < 1) {
                 int y = currentYear - 1;
                 load(y, 12);
             } else {
                 load(currentYear, m);
             }
         }
 
     }
 
     public void onChange(Widget sender) {
         ListBox listBox = (ListBox) sender;
 
         String value = Util.getSelected(listBox);
         String[] monthYear = value.split("/");
 
         int year = Integer.parseInt(monthYear[0]);
         int month = Integer.parseInt(monthYear[1]);
 
         load(year, month);
 
     }
 
     private void load(int year, int month) {
         accountDetailLinesHelper.init();
 
         currentYear = 0;
         currentMonth = 0;
 
         AuthResponder.get(constants, messages, me, "accounting/showmonthpost.php?year=" + year
                 + "&month=" + month);
     }
 
 }
