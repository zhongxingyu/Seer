 package org.eclipse.gmt.modisco.jm2t.internal.ui.viewers;
 
 import java.util.List;
 
 import org.eclipse.gmt.modisco.jm2t.core.IJM2TProject;
 import org.eclipse.gmt.modisco.jm2t.core.generator.IGeneratorConfiguration;
 import org.eclipse.gmt.modisco.jm2t.internal.ui.Messages;
 import org.eclipse.jface.viewers.ColumnWeightData;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.LabelProviderChangedEvent;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TableLayout;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 
 public class GeneratorConfigurationComposite extends
 		AbstractTableListComposite<IGeneratorConfiguration> {
 	protected IGeneratorConfiguration selection;
 	protected IGeneratorConfiguration defaultGeneratorConfiguration;
 	protected GeneratorConfigurationSelectionListener listener;
 
 	public interface GeneratorConfigurationSelectionListener {
 		public void generatorConfigurationSelected(
 				IGeneratorConfiguration generatorConfiguration);
 	}
 
 	class GeneratorConfigurationViewerSorter extends ViewerSorter {
 		boolean sortByName;
 
 		public GeneratorConfigurationViewerSorter(boolean sortByName) {
 			this.sortByName = sortByName;
 		}
 
 		public int compare(Viewer viewer, Object e1, Object e2) {
 			IGeneratorConfiguration r1 = (IGeneratorConfiguration) e1;
 			IGeneratorConfiguration r2 = (IGeneratorConfiguration) e2;
 			// if (sortByName)
 			return getComparator().compare(notNull(r1.getName()),
 					notNull(r2.getName()));
 
 			// if (r1.getGeneratorConfigurationType() == null)
 			// return -1;
 			// if (r2.getGeneratorConfigurationType() == null)
 			// return 1;
 			// return getComparator().compare(
 			// notNull(r1.getGeneratorConfigurationType().getName()),
 			// notNull(r2.getGeneratorConfigurationType().getName()));
 		}
 
 		protected String notNull(String s) {
 			if (s == null)
 				return "";
 			return s;
 		}
 	}
 
 	public GeneratorConfigurationComposite(IJM2TProject project,
 			Composite parent, int style,
 			GeneratorConfigurationSelectionListener listener2) {
 		super(parent, style);
 		this.listener = listener2;
 
 		TableLayout tableLayout = new TableLayout();
 		table.setLayout(tableLayout);
 		table.setHeaderVisible(true);
 
 		tableLayout.addColumnData(new ColumnWeightData(0, 160, true));
 		TableColumn col = new TableColumn(table, SWT.NONE);
 		col.setText(Messages.columnName);
 		col.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				tableViewer.setSorter(new GeneratorConfigurationViewerSorter(
 						true));
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 		});
 
 		tableLayout.addColumnData(new ColumnWeightData(0, 125, true));
 		col = new TableColumn(table, SWT.NONE);
 		col.setText(Messages.columnType);
 		col.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				tableViewer.setSorter(new GeneratorConfigurationViewerSorter(
 						false));
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 		});
 
 		List<IGeneratorConfiguration> generatorConfigurations = super
 				.getElements();
 		generatorConfigurations.addAll(project.readGeneratorConfigurations());
 		tableViewer
 				.setContentProvider(new GeneratorConfigurationContentProvider(
 						generatorConfigurations));
 
 		ILabelProvider labelProvider = new GeneratorConfigurationTableLabelProvider();
 		labelProvider.addListener(new ILabelProviderListener() {
 			public void labelProviderChanged(LabelProviderChangedEvent event) {
 				Object[] obj = event.getElements();
 				if (obj == null)
 					tableViewer.refresh(true);
 				else {
 					// obj = ServerUIPlugin.adaptLabelChangeObjects(obj);
 					int size = obj.length;
 					for (int i = 0; i < size; i++)
 						tableViewer.refresh(obj[i], true);
 				}
 			}
 		});
 		tableViewer.setLabelProvider(labelProvider);
 
 		tableViewer.setInput(AbstractTreeContentProvider.ROOT);
 		tableViewer.setColumnProperties(new String[] { "name", "type" });
 		tableViewer.setSorter(new GeneratorConfigurationViewerSorter(true));
 
 		tableViewer
 				.addSelectionChangedListener(new ISelectionChangedListener() {
 					public void selectionChanged(SelectionChangedEvent event) {
 						Object obj = getSelection(event.getSelection());
 						if (obj instanceof IGeneratorConfiguration)
 							selection = (IGeneratorConfiguration) obj;
 						else
 							selection = null;
 						listener.generatorConfigurationSelected(selection);
 					}
 				});
 
 		// table.addKeyListener(new KeyListener() {
 		// public void keyPressed(KeyEvent e) {
 		// if (e.character == 'l') {
 		// try {
 		// IGeneratorConfiguration generatorConfiguration =
 		// getSelectedGeneratorConfiguration();
 		// IGeneratorConfigurationWorkingCopy wc = generatorConfiguration
 		// .createWorkingCopy();
 		// wc.setReadOnly(!generatorConfiguration.isReadOnly());
 		// wc.save(false, null);
 		// refresh(generatorConfiguration);
 		// } catch (Exception ex) {
 		// // ignore
 		// }
 		// }
 		// }
 		//
 		// public void keyReleased(KeyEvent e) {
 		// // do nothing
 		// }
 		// });
 
 		// after adding an item do the packing of the table
 		if (table.getItemCount() > 0) {
 			TableColumn[] columns = table.getColumns();
 			for (int i = 0; i < columns.length; i++)
 				columns[i].pack();
 			table.pack();
 		}
 	}
 
 	protected void createTable() {
 		table = new Table(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL
				| SWT.H_SCROLL | SWT.MULTI);
 	}
 
 	protected void createTableViewer() {
 		tableViewer = new TableViewer(table);
 	}
 
 }
