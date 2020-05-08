 package albert.lacambra.client.mocks;
 
 import java.util.Date;
 
 import albert.lacambra.client.models.DTOBudget;
 import albert.lacambra.client.models.IndividualCostDTO;
 import albert.lacambra.client.models.PeriodicCostDTO;
 import albert.lacambra.shared.models.PeriodStep;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.event.dom.client.BlurEvent;
 import com.google.gwt.event.dom.client.BlurHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.FocusEvent;
 import com.google.gwt.event.dom.client.FocusHandler;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RadioButton;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 public class InputForms {
 
 	VerticalPanel allFormsPanel = new VerticalPanel();
 	VerticalPanel periodicCostFormPanel = new VerticalPanel();
 	VerticalPanel individualCostFormPanel = new VerticalPanel();
 	VerticalPanel budgetsPanel = new VerticalPanel();
 
 	public InputForms(){
 
 		allFormsPanel.add(periodicCostFormPanel);
 		allFormsPanel.add(new Label("---------------------"));
 		allFormsPanel.add(individualCostFormPanel);
 		allFormsPanel.add(new Label("---------------------"));
 		allFormsPanel.add(budgetsPanel);
 
 		buildPeriodicCostForm();
 		buildBudgetForm();
 		buildIndividualCostForm();
 
 	}
 
 	public Widget getWidget() {
 		return allFormsPanel;
 	}
 
 	private void buildPeriodicCostForm() {
 
 		periodicCostFormPanel.add(new Label("Periodic cost"));
 		
 		final DefaultTextTextBox concept = new DefaultTextTextBox("concept");
 		final DefaultTextTextBox cost = new DefaultTextTextBox("cost");
 		final DatePanel startDatePanel = new DatePanel("Start");
 		final DatePanel endDatePanel = new DatePanel("End");
 		final PeriodStepPanel periodStepPanel = new PeriodStepPanel();
 		final LabeledCheckbox labeledCheckbox = new LabeledCheckbox("Fixed cost?");
 		final Button ok = new Button("save");
 		
 		ok.addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				PeriodicCostDTO costDTO = new PeriodicCostDTO();
 				costDTO.setConcept(concept.getText())
 					.setCost(Integer.parseInt(cost.getText()))
 					.setEnd(endDatePanel.getDate())
 					.setStart(startDatePanel.getDate())
 					.setIsFixedCost(labeledCheckbox.isChecked())
 					.setPeriodStep(periodStepPanel.getPeriodStep());
 				
 				Log.info(costDTO.serializeToJsonValue().toString());
 				
 			}
 		});
 
 		periodicCostFormPanel.add(concept);
 		periodicCostFormPanel.add(cost);
 		periodicCostFormPanel.add(startDatePanel);
 		periodicCostFormPanel.add(endDatePanel);
 		periodicCostFormPanel.add(periodStepPanel);
 		periodicCostFormPanel.add(labeledCheckbox);
 		periodicCostFormPanel.add(ok);
 
 	}
 	
 	private void buildIndividualCostForm() {
 
 		individualCostFormPanel.add(new Label("Individual cost"));
 		
 		final DefaultTextTextBox concept = new DefaultTextTextBox("concept");
 		final DefaultTextTextBox cost = new DefaultTextTextBox("cost");
 		final DatePanel datePanel = new DatePanel();
 		final Button ok = new Button("save");
 		
 		ok.addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				IndividualCostDTO costDTO = new IndividualCostDTO();
 				costDTO.setConcept(concept.getText())
					.setCost(Integer.parseInt(cost.getText()));
 				
 				Log.info(costDTO.serializeToJsonValue().toString());
 				
 			}
 		});
 
 		individualCostFormPanel.add(concept);
 		individualCostFormPanel.add(cost);
 		individualCostFormPanel.add(datePanel);
 		individualCostFormPanel.add(ok);
 
 	}
 	
 	private void buildBudgetForm() {
 		
 		budgetsPanel.add(new Label("Budget"));
 		
 		final DefaultTextTextBox name = new DefaultTextTextBox("name");
 		final DefaultTextTextBox amount = new DefaultTextTextBox("amount");
 		final DefaultTextTextBox year = new DefaultTextTextBox("YYYY");
 		final Button ok = new Button("save");
 		
 		ok.addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				DTOBudget dtoBudget = new DTOBudget()
 					.setAmount(Integer.parseInt(amount.getText()))
 					.setYear(Integer.parseInt(year.getText()))
 					.setName(name.getText());
 				
 				Log.info(dtoBudget.serializeToJsonValue().toString());
 			}
 		});
 		
 		budgetsPanel.add(name);
 		budgetsPanel.add(amount);
 		budgetsPanel.add(year);
 		budgetsPanel.add(ok);
 		
 	}
 
 	private class LabeledCheckbox extends HorizontalPanel {
 
 		CheckBox box = new CheckBox();
 
 		public LabeledCheckbox(String text) {
 
 			add(new Label(text));
 			add(box);
 		}
 
 		public boolean isChecked() {
 			return box.getValue();
 		}
 
 	}
 
 	public class PeriodStepPanel extends VerticalPanel{
 
 		PeriodStep periodStep = null;
 
 		public PeriodStepPanel() {
 
 						PeriodStep[] periodSteps = PeriodStep.values();
 						
 						for ( int i = 0; i < periodSteps.length; i++ ) {
 							
 							RadioButton radioButton = new RadioButton("periodsteps");
 							final PeriodStep currentPeriodStep = periodSteps[i];
 							
 							radioButton.addClickHandler(new ClickHandler() {
 								
 								@Override
 								public void onClick(ClickEvent event) {
 									periodStep = currentPeriodStep;
 								}
 							});
 							
 							HorizontalPanel panel = new HorizontalPanel();
 							add(panel);
 							panel.add(new Label(periodSteps[i].name()));
 							panel.add(radioButton);
 					}
 		}
 
 		public PeriodStep getPeriodStep() {
 			return periodStep;
 		}
 
 	}
 
 	private class DatePanel extends HorizontalPanel {
 
 		DefaultTextTextBox day = new DefaultTextTextBox("DD");
 		DefaultTextTextBox month = new DefaultTextTextBox("MM");
 		DefaultTextTextBox year = new DefaultTextTextBox("YYYY");
 
 		public DatePanel(String label) {
 			add(new Label(label));
 			add(day);
 			add(month);
 			add(year);
 		}
 		
 		public DatePanel() {
 			add(day);
 			add(month);
 			add(year);
 		}
 
 		public Long getDate() {
 
 			DateTimeFormat timeFormat = DateTimeFormat.getFormat("dd-MM-yyyy");
 			String yearStr = year.getText().length() == 4 ? year.getText() : "20" + year.getText();
 			String monthStr = month.getText().length() == 2 ? month.getText() : "0" + month.getText();
 			String dayStr = day.getText().length() == 2 ? day.getText() : "0" + day.getText();
 			Date d = timeFormat.parse(dayStr + "-" + monthStr + "-" + yearStr);
 
 			return d.getTime();
 		}
 
 		public void setDate(Long time) {
 			DateTimeFormat timeFormat = DateTimeFormat.getFormat("dd-MM-yyyy");
 			String fDate = timeFormat.format(new Date(time));
 			String[] date = fDate.split("-");
 
 			day.setText(date[0]);
 			month.setText(date[1]);
 			year.setText(date[2]);
 		}
 	}
 
 	private class DefaultTextTextBox extends TextBox {
 
 		public DefaultTextTextBox(final String text) {
 			
 			setText(text);
 			
 			addFocusHandler(new FocusHandler() {
 
 				@Override
 				public void onFocus(FocusEvent event) {
 					if (getText().equals(text)) {
 						setText("");
 					}
 				}
 			});
 
 			addBlurHandler(new BlurHandler() {
 
 				@Override
 				public void onBlur(BlurEvent event) {
 					if ( getText().equals("") ){
 						setText(text);
 					}
 				}
 			});
 		}
 	}
 
 
 }
