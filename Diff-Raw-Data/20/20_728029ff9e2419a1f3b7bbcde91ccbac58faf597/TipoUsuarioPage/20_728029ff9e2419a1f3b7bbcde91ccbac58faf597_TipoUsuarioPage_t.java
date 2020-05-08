 package ar.noxit.ehockey.web.pages.usuarios;
 
 import java.util.List;
 
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import ar.noxit.ehockey.web.pages.base.AbstractContentPage;
 import ar.noxit.exceptions.NoxitException;
 import ar.noxit.web.wicket.model.LDM;
 
 public class TipoUsuarioPage extends AbstractContentPage{
 
     @SpringBean
     private IUsuarioDTOProvider provider;
     private String selected;
 
     public TipoUsuarioPage() {
 
        add(new FeedbackPanel("feedback"));
        Form<Void> form = new Form<Void>("form"){
 
             @Override
             protected void onSubmit() {
                 setResponsePage(new AltaUsuarioPage(new Model<UsuarioDTO> (provider.createUsuarioDTO(selected))));
             }
        };

        form.add(new DropDownChoice<String>("tipos", new PropertyModel<String>(this, "selected"), new UsuarioListTypesModel()));
        add(form);
     }
 
     public class UsuarioListTypesModel extends LDM<List<String>> {
 
         @Override
         protected List<String> doLoad() throws NoxitException {
             return provider.getListaTipos();
         }
         
     }
 }
