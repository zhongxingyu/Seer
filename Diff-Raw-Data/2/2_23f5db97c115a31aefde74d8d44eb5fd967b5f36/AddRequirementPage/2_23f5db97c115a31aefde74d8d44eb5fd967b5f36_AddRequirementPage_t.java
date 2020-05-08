 package net.alpha01.jwtest.pages.requirement;
 
 import java.io.Serializable;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import net.alpha01.jwtest.beans.Requirement;
 import net.alpha01.jwtest.beans.RequirementType;
 import net.alpha01.jwtest.dao.RequirementMapper;
 import net.alpha01.jwtest.dao.RequirementMapper.Dependency;
 import net.alpha01.jwtest.dao.RequirementMapper.RequirementSelectSort;
 import net.alpha01.jwtest.dao.SqlConnection;
 import net.alpha01.jwtest.dao.SqlSessionMapper;
 import net.alpha01.jwtest.pages.LayoutPage;
 
 import org.apache.ibatis.exceptions.PersistenceException;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
 import org.apache.wicket.authroles.authorization.strategies.role.Roles;
 import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.ListMultipleChoice;
 import org.apache.wicket.markup.html.form.TextArea;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 
 @AuthorizeInstantiation(value = { Roles.ADMIN, "PROJECT_ADMIN", "TESTER", "MANAGER" })
 public class AddRequirementPage extends LayoutPage implements Serializable {
 	private static final long serialVersionUID = 1L;
 	private Requirement req = new Requirement();
 	private Model<RequirementType> typeModel = new Model<RequirementType>(new RequirementType());
 	private ArrayList<Requirement> dependencies = new ArrayList<Requirement>();
 	private ListMultipleChoice<Requirement> dependencyFld;
 	private TextField<BigInteger> numFld;
 
 	public AddRequirementPage(PageParameters params) {
 		super(params);
 
 		add(new Label("projectName", getSession().getCurrentProject().getName()));
 		Form<Requirement> addForm = new Form<Requirement>("addForm") {
 			private static final long serialVersionUID = 1L;
 
 			protected void onSubmit() {
 				SqlSessionMapper<RequirementMapper> sesAddMapper = SqlConnection.getSessionMapper(RequirementMapper.class);
 				req.setId_project(AddRequirementPage.this.getSession().getCurrentProject().getId());
 
 				req.setId_type(typeModel.getObject().getId());
 				try {
 					if (sesAddMapper.getMapper().add(req).equals(1)) {
 						// aggiungo le dipendenze
 						Iterator<Requirement> itr = dependencies.iterator();
 						while (itr.hasNext()) {
 							Requirement r = itr.next();
 							try {
 								sesAddMapper.getMapper().addDependency(new Dependency(req.getId(), r.getId()));
 							} catch (PersistenceException e) {
 
 							}
 						}
 						sesAddMapper.commit();
 						sesAddMapper.close();
 						info("Requirement added successfully");
 						setResponsePage(AddRequirementPage.class, new PageParameters().add("currType", typeModel.getObject().getId()));
 					} else {
 						error("SQL ERROR :Requirement not addedd");
 					}
 				} catch (PersistenceException e) {
 					error("SQL ERROR: Duplicate PKEY");
 				}
 			};
 		};
 		SqlSessionMapper<RequirementMapper> sesMapper = SqlConnection.getSessionMapper(RequirementMapper.class);
 		// recupero i requisiti per le dipendenze
 		List<Requirement> allReq = Collections.emptyList();
 		dependencyFld = new ListMultipleChoice<Requirement>("dependencyFld", new Model<ArrayList<Requirement>>(dependencies), allReq);
 		dependencyFld.setOutputMarkupId(true);
 		addForm.add(dependencyFld);
 
 		List<RequirementType> types = sesMapper.getMapper().getTypes();
 
 		numFld = new TextField<BigInteger>("numFld", new PropertyModel<BigInteger>(req, "num"));
 		numFld.setOutputMarkupId(true);
 		numFld.setRequired(true);
 
 		if (!params.get("currType").isNull()) {
 			RequirementType prevType = sesMapper.getMapper().getType(BigInteger.valueOf(params.get("currType").toLong()));
 			typeModel.setObject(prevType);
 			refreshDependencies(null);
 		}
 		sesMapper.close();
 
 		DropDownChoice<RequirementType> typeFld = new DropDownChoice<RequirementType>("typeFld", typeModel, types);
 		typeFld.setRequired(true);
 		typeFld.add(new AjaxFormComponentUpdatingBehavior("onchange") {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void onUpdate(AjaxRequestTarget ajaxTarget) {
 				refreshDependencies(ajaxTarget);
 			}
 		});
 		addForm.add(typeFld);
 
 		addForm.add(numFld);
		addForm.add(new TextField<String>("nameFld", new PropertyModel<String>(req, "name")).setRequired(true));
 		addForm.add(new TextArea<String>("descriptionFld", new PropertyModel<String>(req, "description")));
 		add(addForm);
 	}
 
 	private void refreshDependencies(AjaxRequestTarget ajaxTarget) {
 		SqlSessionMapper<RequirementMapper> sesMapper = SqlConnection.getSessionMapper(RequirementMapper.class);
 		HashMap<String, Object> qryParams = new HashMap<String, Object>();
 		qryParams.put("idType", typeModel.getObject().getId());
 		qryParams.put("idProject", AddRequirementPage.this.getSession().getCurrentProject().getId());
 		req.setNum(BigInteger.valueOf(sesMapper.getMapper().nextNum(qryParams) != null ? sesMapper.getMapper().nextNum(qryParams) : 1));
 
 		List<Requirement> allReq = sesMapper.getMapper().getAll(new RequirementSelectSort(getSession().getCurrentProject().getId(), typeModel.getObject().getId(), "name", true));
 		dependencyFld.setChoices(allReq);
 
 		sesMapper.close();
 		if (ajaxTarget != null) {
 			ajaxTarget.add(dependencyFld);
 			ajaxTarget.add(numFld);
 		}
 	}
 }
