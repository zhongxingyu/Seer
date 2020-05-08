 package net.jorgeherskovic.medrec.client;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import net.jorgeherskovic.medrec.client.event.RedrawEvent;
 import net.jorgeherskovic.medrec.client.event.RowDroppedEvent;
 import net.jorgeherskovic.medrec.shared.Consolidation;
 import net.jorgeherskovic.medrec.shared.Medication;
 import net.jorgeherskovic.medrec.shared.ReconciledMedication;
 
 import org.adamtacy.client.ui.effects.events.EffectCompletedEvent;
 import org.adamtacy.client.ui.effects.events.EffectCompletedHandler;
 import org.adamtacy.client.ui.effects.impl.Fade;
 
 import com.google.gwt.event.shared.SimpleEventBus;
 import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
 
 public class ConsolidatedRenderer extends TableRenderer {
 	private static String[] columnStyles = { "DragHandle", "Origin",
 			"Medication", "Dosage", "Frequency", "Date", "Date", "Form",
 			"Relation" };
 
 	public ConsolidatedRenderer(DraggableFlexTable table, String[] headings,
 			SimpleEventBus bus) {
 		super(table, headings, bus);
 	}
 
 	private void makeCellDoubleHeight(int row, int col) {
 		FlexCellFormatter cf = this.getAttachedTable().getFlexCellFormatter();
 
 		this.getAttachedTable().removeCell(row + 1, col);
 		cf.setRowSpan(row, col, 2);
 	}
 	
 	private void flattenCell(int row, int col) {
 		CellFormatter cf = this.getAttachedTable().getCellFormatter();
 		// this.getAttachedTable().getCellFormatter().setHeight(row, col, "0");
 		cf.removeStyleName(row, col, "NoReconciliation");
 		cf.removeStyleName(row, col, "PartialReconciliation");
 		cf.addStyleName(row, col, "FullReconciliation");
 	}
 
 	private String getReconciliationStyle(Consolidation cons) {
 		String thisStyle = "NoReconciliation";
 		if (cons.getScore() > 0.1) {
 			thisStyle = "PartialReconciliation";
 		}
 		if (cons.getScore() > 0.99) {
 			thisStyle = "FullReconciliation";	
 		}
 		
 		return thisStyle;
 	}
 	
 	// returns the handle to the drag token
 	private HTML renderParsedRow(DraggableFlexTable t, int rownum, Consolidation this_cons, Medication m) {
 		int col = 0;
 
 		HTML handle = new HTML(this.dragToken);
 		t.setWidget(rownum, col++, handle);
 		t.getRowDragController().makeDraggable(handle);
 
 		t.setHTML(rownum, col++, m.getProvenance());
 
 		t.setHTML(rownum, col++, m.getMedicationName());
 		String dosage1 = m.getDose() + " " + m.getUnits();
 		t.setHTML(rownum, col++, dosage1);
 		t.setHTML(rownum, col++, m.getInstructions());
 		t.setHTML(rownum, col++, m.getStartDateString());
 		t.setHTML(rownum, col++, m.getEndDateString());
 		t.setHTML(rownum, col++, m.getFormulation());
 		t.setText(rownum, col++, this_cons.getExplanation());
 
 		String thisStyle = getReconciliationStyle(this_cons);
 		t.getRowFormatter().addStyleName(rownum, thisStyle);
 
 		String cellFormatStyle;
 
 		if (this_cons.length == 1) {
 			cellFormatStyle = "SingleRowDesign";
 		} else {
 			cellFormatStyle = "MultiRowTopDesign";
 		}
 
 		t.getRowFormatter().addStyleName(rownum, cellFormatStyle);
 
 		/*
 		 * Apply the TableDesign style to each cell individually to get
 		 * borders
 		 */
 		this.applyStyleToAllCellsInRow(rownum, cellFormatStyle);
 		this.applyStyleArrayToRow(rownum, columnStyles);
 
 		return handle;
 	}
 	
 	private HTML renderUnparsedRow(DraggableFlexTable t, int rownum, Consolidation this_cons, Medication m) {
 		int col = 0;
 
 		HTML handle = new HTML(this.dragToken);
 		t.setWidget(rownum, col++, handle);
 		t.getRowDragController().makeDraggable(handle);
 
 		t.setHTML(rownum, col++, m.getProvenance());
 		t.setHTML(rownum, col++, m.getOriginalString());
 		
 		String cellFormatStyle;
 		if (this_cons.length == 1) {
 			cellFormatStyle = "SingleRowDesign";
 		} else {
 			cellFormatStyle = "MultiRowTopDesign";
 		}
 		
 		t.getRowFormatter().addStyleName(rownum, getReconciliationStyle(this_cons));
 		
 		this.applyStyleToAllCellsInRow(rownum, cellFormatStyle);
 		this.applyStyleArrayToRow(rownum, columnStyles);
 		// Discover the width of the table headings
 		//int headerWidth=0;
 		
 		//for (int i = col; i<t.getCellCount(0); i++) {
 		//	t.addCell(rownum);
 		//}
 		
 		// Merge all cells to the right of the 
 		t.getFlexCellFormatter().setColSpan(rownum, col - 1, t.getCellCount(0) - 3);
 		t.setHTML(rownum, col++, this_cons.getExplanation());
 		
 		return handle;
 	}
 	
 	private HTML renderRow(DraggableFlexTable t, int rownum, Consolidation cons, Medication m) {
 		if (m.isParsed())
 			return renderParsedRow(t, rownum, cons, m);
 		
 		return renderUnparsedRow(t, rownum, cons, m);
 	}
 
 	@Override
 	public void renderTable() {
 		DraggableFlexTable t = this.getAttachedTable();
 		List<Consolidation> cons = t.getMedList();
 
 		t.removeAllRows();
 		Map<HTML, Consolidation> rowMapping = t.getRowMapping();
 		rowMapping.clear();
 		t.clearTargetRows();
 
 		/* In this case, the "rows to remove" array contains the consolidations to remove. Awful semantics, I know, and they require fixing later. */
 		// TODO: Fix row removal semantics
 		
 		this.renderTableHeadings("TableHeading");
 		int currentRow = 1;
 
 		LinkedList<Fade> my_fades=new LinkedList<Fade>();
 		
 		for (int i = 0; i < cons.size(); i++) {
 			ReconciledMedication this_cons = new ReconciledMedication(
 					cons.get(i), 0);
 			Medication m1 = this_cons.getMed1();
 			Medication m2 = this_cons.getMed2();
 			
 			HTML handle= renderRow(t, currentRow, this_cons, m1);
 
 			if (t.isRowRemovable(i)) {
 				my_fades.add(new Fade(t.getRowFormatter().getElement(currentRow)));
 			}
 			
 			t.setTargetRow(currentRow, currentRow);
 			rowMapping.put(handle, this_cons);
 			
 			int col = t.getCellCount(currentRow);
 			currentRow += 1;
 			
 			if (!m2.isEmpty()) {
 				String thisStyle = getReconciliationStyle(this_cons);
 				// So there *is* another row in this group.
 				t.setTargetRow(currentRow-1, currentRow);
 				t.setTargetRow(currentRow, currentRow);
 				
 				// Pre-build the current row so resizing works properly
 				for (int j = 0; j < col; j++) {
 					t.setHTML(currentRow, j, "&nbsp;");
 				}
 
 				col = 0;
 				
 				ArrayList<Integer> cells_to_squish = new ArrayList<Integer>();
 
 				HTML second_handle = new HTML();
 				second_handle.setHTML(this.dragToken);
 				Consolidation reverse_cons = new ReconciledMedication(
 						this_cons, 1);
 
 				t.setWidget(currentRow, col++, second_handle);
 				t.getRowDragController().makeDraggable(second_handle);
 
 				// t.setText(currentRow, col++, Integer.toString(i + 1)); // No
 				// entry number
 
 				if (m2.getProvenance().equals(m1.getProvenance())) {
 					// t.addCell(currentRow);
 					// flattenCell(currentRow, col);
 					// makeCellDoubleHeight(t, currentRow - 1, col++);
 					cells_to_squish.add(col++);
 				} else {
 					t.setHTML(currentRow, col++, m2.getProvenance());
 				}
 
 				if (m2.isParsed()) {
 					if (m2.getMedicationName().equals(m1.getMedicationName())) {
 						// t.addCell(currentRow);
 						// flattenCell(currentRow, col);
 						// makeCellDoubleHeight(t, currentRow - 1, col++);
 						cells_to_squish.add(col++);
 					} else {
 						t.setHTML(currentRow, col++, m2.getMedicationName());
 					}
 					String dosage2 = m2.getDose() + " " + m2.getUnits();
 					String dosage1 = m1.getDose() + " " + m1.getUnits();
 					if (dosage2.equals(dosage1)) {
 						// t.addCell(currentRow);
 						// flattenCell(currentRow, col);
 						// makeCellDoubleHeight(t, currentRow - 1, col++);
 						cells_to_squish.add(col++);
 					} else {
 						t.setHTML(currentRow, col++, dosage2);
 					}
 					if (m2.getInstructions().equals(m1.getInstructions())) {
 						// t.addCell(currentRow);
 						// flattenCell(currentRow, col);
 						// makeCellDoubleHeight(t, currentRow - 1, col++);
 						cells_to_squish.add(col++);
 					} else {
 						t.setHTML(currentRow, col++, m2.getInstructions());
 					}
 					if (m2.getStartDateString().equals(m1.getStartDateString())) {
 						// t.addCell(currentRow);
 						// flattenCell(currentRow, col);
 						// makeCellDoubleHeight(t, currentRow - 1, col++);
 						cells_to_squish.add(col++);
 					} else {
 						t.setHTML(currentRow, col++, m2.getStartDateString());
 					}
 					if (m2.getEndDateString().equals(m1.getEndDateString())) {
 						// t.addCell(currentRow);
 						// flattenCell(currentRow, col);
 						// makeCellDoubleHeight(t, currentRow - 1, col++);
 						cells_to_squish.add(col++);
 					} else {
 						t.setHTML(currentRow, col++, m2.getEndDateString());
 					}
 					if (m2.getFormulation().equals(m1.getFormulation())) {
 						// t.addCell(currentRow);
 						// flattenCell(currentRow, col);
 						// makeCellDoubleHeight(t, currentRow - 1, col++);
 						cells_to_squish.add(col++);
 					} else {
 						t.setHTML(currentRow, col++, m2.getFormulation());
 					}
 					// cells_to_squish.add(col);
 					// t.setText(currentRow, col++, this_cons.getExplanation());
 					// t.addCell(currentRow);
 					cells_to_squish.add(col++);
 					// makeCellDoubleHeight(t, currentRow - 1, col++); // Make
 					// explanation
 					// double height
 				} else {
 					// Not parsed
 					if (m2.getOriginalString().equals(m1.getOriginalString())) {
 						// t.addCell(currentRow);
 						// flattenCell(currentRow, col);
 						// makeCellDoubleHeight(t, currentRow - 1, col++);
 						cells_to_squish.add(col++);
 					} else {
 						t.setHTML(currentRow, col++, m2.getOriginalString());
 					}
 					t.getFlexCellFormatter().setColSpan(currentRow, col - 1, t.getCellCount(0) - 3);
 					cells_to_squish.add(col++);
 				}
 				t.getRowFormatter().addStyleName(currentRow,
 						"MultiRowBottomDesign");
 				t.getRowFormatter().addStyleName(currentRow, thisStyle);
 				rowMapping.put(second_handle, reverse_cons);
 
 				/*
 				 * Apply the TableDesign style to each cell individually to get
 				 * borders
 				 */
 				this.applyStyleToAllCellsInRow(currentRow,
 						"MultiRowBottomDesign");
 				this.applyStyleArrayToRow(currentRow, columnStyles);
 
 				for (int j = cells_to_squish.size() - 1; j >= 0; j--) {
 					flattenCell(currentRow, cells_to_squish.get(j));
 					makeCellDoubleHeight(currentRow - 1, cells_to_squish.get(j));
 				}
 
 
 				if (t.isRowRemovable(i)) {
 					my_fades.add(new Fade(t.getRowFormatter().getElement(currentRow)));
 				}
 	
 				currentRow += 1;
 			}
 			if (t.getRowCount() > 1) {
 				t.setTargetRow(0, 0);
 			}
 		}
 
 		// Empty row removal list
 		int rr=t.getRowToRemove();
 		while (rr > -1) {
 			t.deleteMed(rr);
 			rr=t.getRowToRemove();
 		}
 		
 		// play fades
 		while (!my_fades.isEmpty()) {
 			Fade this_fade=my_fades.remove();
 			this_fade.addEffectCompletedHandler(new EffectCompletedHandler() {
 				public void onEffectCompleted(EffectCompletedEvent evt) {
 					ConsolidatedRenderer.this.bus.fireEvent(new RedrawEvent());
 				}
 			});
 			this_fade.setDuration(TableRenderer.FADE_DURATION);
 			this_fade.play();
 		}
 		
 		return;
 
 	}
 
 	@Override
 	public void handleDroppedRow(RowDroppedEvent event) {
 		HTML handle = (HTML) event.getSourceTable().getWidget(
 				event.getSourceRow(), 0);
 		Consolidation cons = event.getSourceTable().getRowMapping().get(handle);
 		Medication selected = cons.getSelectedMedication();
 		if (selected == null) {
 			selected = cons.getMed1();
 		}
 		// Window.alert("Dragged medication:" +
 		// selected.getMedicationName()+" "+selected.getDose());
 		// Someone has dropped a row on the Consolidated table. We need to add
 		// *all* original rows to our internal representation.
 		List<? extends Consolidation> myMedList = this.getAttachedTable()
 				.getMedList();
 		// count until we find the real target position
 		int targetPosition = event.getDestRow() - 1;
 		int realTargetPosition = 0;
 		int tableTargetPosition = 0;
 
 		if (targetPosition > 0) {
 			while (tableTargetPosition < targetPosition) {
 				tableTargetPosition += myMedList.get(realTargetPosition).length;
 				realTargetPosition++;
 			}
 		}
 
 		this.getAttachedTable().insertMed(realTargetPosition, cons);
 		//event.getSourceTable().deleteMed(event.getSourceRow() - 1);
 		event.getSourceTable().registerRemovalRequest(event.getSourceRow()-1);
 		//ArrayList<Fade> fades=new ArrayList<Fade>();
 		//Fade new_fade=new Fade(event.getSourceTable().getRowFormatter().getElement(0));
 		//new_fade.play();
 		
 //		Fade fade_it=new Fade(event.getSourceTable().getWidget(row, column))
 		bus.fireEvent(new RedrawEvent());
 	}
 
 }
