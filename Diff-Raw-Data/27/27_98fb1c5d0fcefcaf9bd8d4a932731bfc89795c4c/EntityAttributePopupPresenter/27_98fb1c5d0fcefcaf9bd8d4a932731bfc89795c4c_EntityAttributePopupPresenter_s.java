 package generators.generated.templates;
 
 public class EntityAttributePopupPresenter extends AbstractExplorerTemplate implements IExplorerTemplate
 {
   protected static String nl;
   public static synchronized EntityAttributePopupPresenter create(String lineSeparator)
   {
     nl = lineSeparator;
     EntityAttributePopupPresenter result = new EntityAttributePopupPresenter();
     nl = null;
     return result;
   }
 
   public final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
   protected final String TEXT_1 = "package au.com.arclight.sag.client.domain.mvp.presenter.explorer.";
   protected final String TEXT_2 = ".popup;" + NL + "" + NL + "import java.util.List;" + NL + "" + NL + "import au.com.arclight.sag.client.domain.model.people.";
   protected final String TEXT_3 = ";" + NL + "import au.com.arclight.sag.client.domain.mvp.presenter.explorer.AbstractExplorerPopupPresenter;" + NL + "import au.com.arclight.sag.client.domain.mvp.view.explorer.";
   protected final String TEXT_4 = ".popup.I";
   protected final String TEXT_5 = "PopupView;" + NL + "" + NL + "import com.google.gwt.user.client.ui.HasOneWidget;" + NL + "import com.google.inject.Inject;" + NL + "" + NL + "public class ";
   protected final String TEXT_6 = "PopupPresenter extends AbstractExplorerPopupPresenter<";
   protected final String TEXT_7 = ">" + NL + "\timplements I";
   protected final String TEXT_8 = "PopupView.Presenter" + NL + "{" + NL + "\tprivate I";
   protected final String TEXT_9 = "PopupView view;" + NL + "\tprivate ";
  protected final String TEXT_10 = " model;" + NL + "" + NL + "\t@Inject" + NL + "\tpublic ";
  protected final String TEXT_11 = "PopupPresenter(I";
  protected final String TEXT_12 = "PopupView view)" + NL + "\t{" + NL + "\t\tthis.view = view;" + NL + "\t\tview.setPresenter(this);" + NL + "\t}" + NL + "" + NL + "\t@Override" + NL + "\tpublic void go(HasOneWidget container) " + NL + "\t{" + NL + "\t\tcontainer.setWidget(view.asWidget());" + NL + "\t}" + NL + "\t" + NL + "\t@Override" + NL + "\tpublic void init(";
  protected final String TEXT_13 = " model)" + NL + "\t{" + NL + "\t\tthis.model = model;" + NL + "\t}" + NL + "" + NL + "\t@Override" + NL + "\tpublic void buttonSaveClicked() " + NL + "\t{" + NL + "\t\t//TODO : Your implementation here to set model results" + NL + "\t\tsaveHandler.handleEvent();" + NL + "\t}" + NL + "}";
 
 	public String generate(String Entity, String Attribute)
   {
     final StringBuffer stringBuffer = new StringBuffer();
     fileName = "PopupPresenter.java";
     String entity = Entity.toLowerCase();
     String entityAttribute = entity + Attribute;
     String EntityAttribute = Entity + Attribute;
     stringBuffer.append(TEXT_1);
     stringBuffer.append(entity);
     stringBuffer.append(TEXT_2);
     stringBuffer.append(Entity);
     stringBuffer.append(TEXT_3);
     stringBuffer.append(entity);
     stringBuffer.append(TEXT_4);
     stringBuffer.append(EntityAttribute);
     stringBuffer.append(TEXT_5);
     stringBuffer.append(EntityAttribute);
     stringBuffer.append(TEXT_6);
     stringBuffer.append(Entity);
     stringBuffer.append(TEXT_7);
     stringBuffer.append(EntityAttribute);
     stringBuffer.append(TEXT_8);
     stringBuffer.append(EntityAttribute);
     stringBuffer.append(TEXT_9);
     stringBuffer.append(Entity);
     stringBuffer.append(TEXT_10);
    stringBuffer.append(EntityAttribute);
     stringBuffer.append(TEXT_11);
     stringBuffer.append(EntityAttribute);
     stringBuffer.append(TEXT_12);
    stringBuffer.append(entity);
     stringBuffer.append(TEXT_13);
     return stringBuffer.toString();
   }
 }
