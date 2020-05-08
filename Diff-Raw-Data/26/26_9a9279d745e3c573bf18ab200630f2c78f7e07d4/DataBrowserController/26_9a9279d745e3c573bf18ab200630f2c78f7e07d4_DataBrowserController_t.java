 package com.bnrdo.databrowser.mvc;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.beans.PropertyChangeEvent;
 import java.util.List;
 import java.util.Random;
 
 import javax.swing.JComboBox;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 
 import ca.odell.glazedlists.FilterList;
 import ca.odell.glazedlists.SortedList;
 import ca.odell.glazedlists.TextFilterator;
 import ca.odell.glazedlists.event.ListEvent;
 import ca.odell.glazedlists.event.ListEventListener;
 import ca.odell.glazedlists.gui.AbstractTableComparatorChooser;
 import ca.odell.glazedlists.gui.TableFormat;
 import ca.odell.glazedlists.swing.TableComparatorChooser;
 import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
 
 import com.bnrdo.databrowser.Pagination;
 import com.bnrdo.databrowser.listener.ModelListener;
 import com.bnrdo.databrowser.listener.PaginationListener;
 import com.bnrdo.databrowser.domain.Person;
 import com.bnrdo.databrowser.exception.ViewException;
 import com.bnrdo.databrowser.mvc.DataBrowserView.PageButton;
 
 @SuppressWarnings("unchecked")
 public class DataBrowserController<E> implements ModelListener {
 	private DataBrowserView view;
 	private DataBrowserModel<E> model;
 	public static int ctr;
 
 	public DataBrowserController(DataBrowserView v, DataBrowserModel<E> m) {
 		view = v;
 		model = m;
 		model.addModelListener(this);
 		ctr = 0;
 	}
 
 	public void control() {
 		setupDataTable();
 		setupFilterBy();
 		setupFilterText();
 	}
 
 	private void setupDataTable() {
 		JTable tblData = view.getDataTable(); 
 		tblData.setModel(model.getDataTableModel());
 		
 		TableComparatorChooser.install(
 				tblData,
 				(SortedList<E>) model.getDataTableSource(),
 				AbstractTableComparatorChooser.SINGLE_COLUMN);
 	}
 
 	private void setupFilterBy() {
 		JComboBox cboFilter = view.getCboSearch();
 		TableFormat<E> tableForm = model.getDataTableFormat();
 		int colCount = tableForm.getColumnCount();
 
 		cboFilter.removeAllItems();
 
 		for (int i = 0; i < colCount; i++) {
 			cboFilter.addItem(tableForm.getColumnName(i));
 		}
 		cboFilter.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent e) {
 				setupFilterText();
 			}
 		});
 	}
 	
 	private void setupFilterText(){
 		final JTextField txtFilter = view.getTxtSearch();
 		
 		FilterList<E> textFilteredSource = new FilterList<E>(model.getDataTableSource(), new TextComponentMatcherEditor<E>(txtFilter, new TextFilterator<E>() {
 			public void getFilterStrings(List baseList, E element) {
				String searchCat = view.getCboSearch().getSelectedItem().toString();
 				Person p = (Person) element;
				//do something like propertyColumnMapping
				if(searchCat.equals("First Name"))
 					baseList.add(p.getFirstName());
				else if(searchCat.equals("Last Name"))
 					baseList.add(p.getLastName());
				else if(searchCat.equals("Birth Day"))
 					baseList.add(p.getBirthDay().toString());
				else if(searchCat.equals("Age"))
 					baseList.add(p.getAge());
				else if(searchCat.equals("Occupation"))
 					baseList.add(p.getOccupation());
 			}
 		}));
 		model.setDataTableSource(textFilteredSource);		
 	}
 	public static int randomNum(){
 		Random r = new Random();
 		return r.nextInt(10);
 	}
 	private void setupPageEvents() {
 		if (view.getBtnFirst() == null
 				&& model.getPagination().getPageNumsExposed().length > 1) {
 			throw new ViewException(
 					"The view for page numbers must be completely setup first before assembling its events");
 		}
 
 		final PageButton[] pageBtns = view.getPageBtns();
 
 		if (model.getPagination().getPageNumsExposed().length > 1) {
 
 			view.getBtnFirst().addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					model.getPagination().setCurrentPageNum(
 							Pagination.FIRST_PAGE);
 				}
 			});
 			view.getBtnPrev().addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					model.getPagination().setCurrentPageNum(
 							Pagination.PREV_PAGE);
 				}
 			});
 			view.getBtnNext().addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					model.getPagination().setCurrentPageNum(
 							Pagination.NEXT_PAGE);
 				}
 			});
 			view.getBtnLast().addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					model.getPagination().setCurrentPageNum(
 							Pagination.LAST_PAGE);
 				}
 			});
 		}
 
 		for (final PageButton btn : pageBtns) {
 			btn.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					int i = Integer.parseInt(btn.getText());
 					model.getPagination().setCurrentPageNum(i);
 				}
 			});
 		}
 
 		// model.getPagination().setCurrentPageNum(
 		// Integer.parseInt(view.getPageBtns()[0].getText()));
 	}
 
 	/* Code when the model has changed some of its property */
 
 	private void pageNumsExposedChanged(int[] newVal) {
 		view.refreshPageNumbers(newVal);
 		setupPageEvents();
 	}
 
 	private void currentPageNumChanged(int newVal) {
 		PageButton[] btns = view.getPageBtns();
 		int lastPageNum = model.getPagination().getLastRawPageNum();
 		int firstPageNum = model.getPagination().getFirstRawPageNum();
 
 		// synch button selection based on currentpagenum from the model and
 		// its text
 
 		for (PageButton btn : btns) {
 			btn.setSelected(Integer.parseInt(btn.getText()) == newVal);
 		}
 
 		view.getBtnFirst().setVisible(!(newVal == firstPageNum));
 		view.getBtnPrev().setVisible(!(newVal == firstPageNum));
 		view.getBtnNext().setVisible(!(newVal == lastPageNum));
 		view.getBtnLast().setVisible(!(newVal == lastPageNum));
 	}
 
 	public void propertyChange(PropertyChangeEvent evt) {
 		String propName = evt.getPropertyName();
 		Object newVal = evt.getNewValue();
 
 		if (DataBrowserModel.FN_PAGINATION.equalsIgnoreCase(propName)) {
 
 			final Pagination newPagination = (Pagination) newVal;
 			pageNumsExposedChanged(newPagination.getPageNumsExposed());
 			currentPageNumChanged(newPagination.getCurrentPageNum());
 
 			newPagination.addPaginationListener(new PaginationListener() {
 				public void pageChanged(int pageNum) {
 					pageNumsExposedChanged(newPagination.getPageNumsExposed());
 					currentPageNumChanged(newPagination.getCurrentPageNum());
 				}
 			});
 		} else if (DataBrowserModel.FN_SEARCH_FILTER_LIST
 				.equalsIgnoreCase(propName)) {
 			control();
 		} else if (DataBrowserModel.FN_DATA_TABLE_MODEL
 				.equalsIgnoreCase(propName)) {
 			control();
 		}
 	}
 }
