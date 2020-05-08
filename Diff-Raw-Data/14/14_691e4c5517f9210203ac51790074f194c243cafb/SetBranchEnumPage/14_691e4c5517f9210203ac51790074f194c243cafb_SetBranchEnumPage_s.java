 package jp.hishidama.xtext.dmdl_editor.ui.wizard.page.operator;
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import jp.hishidama.eclipse_plugin.asakusafw_wrapper.operator.OperatorType;
 import jp.hishidama.eclipse_plugin.jface.ModifiableTable;
 import jp.hishidama.eclipse_plugin.wizard.page.EditWizardPage;
 
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Text;
 
 public class SetBranchEnumPage extends EditWizardPage {
 
 	private IType type;
 
 	private List<String> existsList;
 
 	private Button button0;
 	private Group group0;
 	private Combo enumCombo;
 
 	private Button button1;
 	private Group group1;
 	private Text commentText;
 	private Text nameText;
 	private EnumTable table;
 
 	public SetBranchEnumPage(IType type, OperatorType opType) {
 		super("SetBranchEnumPage");
 
 		setTitle(MessageFormat.format("{0}の出力の列挙型の指定", opType.getName()));
 		setDescription("出力に使用する列挙クラスを指定して下さい。");
 
 		this.type = type;
 	}
 
 	@Override
 	protected Composite createComposite(Composite parent) {
 		Composite composite = new Composite(parent, SWT.NONE);
 		{
 			composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 			composite.setLayout(new GridLayout(2, false));
 		}
 
 		{
 			button0 = new Button(composite, SWT.RADIO);
 			button0.setText("既存の列挙型から選択：");
 
 			group0 = new Group(composite, SWT.SHADOW_IN);
 			group0.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 			group0.setLayout(new GridLayout());
 			existsList = getExistsEnumList();
 			enumCombo = createCombo(group0, 1);
 			enumCombo.setItems(existsList.toArray(new String[existsList.size()]));
 		}
 		{
 			button1 = new Button(composite, SWT.RADIO);
 			button1.setText("新しい列挙型を作成：");
 
 			group1 = new Group(composite, SWT.SHADOW_IN);
 			group1.setLayoutData(new GridData(GridData.FILL_BOTH));
 			group1.setLayout(new GridLayout(2, false));
 			commentText = createTextField(group1, 1, "コメント：");
 			nameText = createTextField(group1, 1, "列挙クラス名：");
 
 			createLabel(group1, "列挙定数：");
 			table = new EnumTable(group1);
 			table.addColumn("name", 128, SWT.NONE);
 			table.addColumn("comment", 256, SWT.NONE);
 
 			createLabel(group1, "");
 			Composite field = new Composite(group1, SWT.NONE);
 			// field.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 			field.setLayout(new FillLayout(SWT.HORIZONTAL));
 			table.createButtonArea(field);
 			table.refresh();
 		}
 
 		button0.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				setEnabled(true, false);
 			}
 		});
 		button1.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				setEnabled(false, true);
 			}
 		});
 
 		if (existsList.isEmpty()) {
 			button1.setSelection(true);
 			setEnabled(false, true);
 		} else {
 			button0.setSelection(true);
 			setEnabled(true, false);
 		}
 
 		return composite;
 	}
 
 	void setEnabled(boolean b0, boolean b1) {
 		group0.setEnabled(b0);
 		enumCombo.setEnabled(b0);
 		group1.setEnabled(b1);
 		commentText.setEnabled(b1);
 		nameText.setEnabled(b1);
 		table.setEnabled(b1);
 		validate(isVisible());
 	}
 
 	private List<String> getExistsEnumList() {
 		List<String> list = new ArrayList<String>();
 		try {
 			for (IType t : type.getTypes()) {
 				if (t.isEnum()) {
 					list.add(t.getElementName());
 				}
 			}
 		} catch (JavaModelException e) {
 			e.printStackTrace();
 		}
 		return list;
 	}
 
 	public static class EnumRow {
 		public String name;
 		public String comment;
 	}
 
 	protected class EnumTable extends ModifiableTable<EnumRow> {
 
 		public EnumTable(Composite parent) {
 			super(parent, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
 		}
 
 		@Override
 		protected String getText(EnumRow element, int columnIndex) {
 			switch (columnIndex) {
 			case 0:
 				return element.name;
 			case 1:
 				return element.comment;
 			default:
 				throw new UnsupportedOperationException("columnIndex=" + columnIndex);
 			}
 		}
 
 		@Override
 		protected void doAdd() {
 			EnumRow row = createElement();
 			BranchEnumDialog dialog = new BranchEnumDialog(getShell(), row);
 			if (dialog.open() != Window.OK) {
 				return;
 			}
 
 			super.doAdd(row);
 		}
 
 		@Override
 		protected EnumRow createElement() {
 			return new EnumRow();
 		}
 
 		@Override
 		protected void editElement(EnumRow element) {
 			BranchEnumDialog dialog = new BranchEnumDialog(getShell(), element);
 			dialog.open();
 		}
 
 		@Override
 		public void refresh() {
 			super.refresh();
 			validate(isVisible());
 		}
 	}
 
 	@Override
 	protected String validate() {
 		if (button0 == null || button1 == null) {
 			return null;
 		}
 
 		if (button0.getSelection()) {
 			String name = enumCombo.getText().trim();
 			if (name.isEmpty()) {
 				return "列挙クラスを選択して下さい。";
 			}
 			return null;
 		}
 		if (button1.getSelection()) {
 			String name = nameText.getText().trim();
 			if (name.isEmpty()) {
 				return "列挙クラス名を入力して下さい。";
 			}
 			if (existsList.contains(name)) {
 				return "列挙クラスが既に存在しています。";
 			}
 
 			List<EnumRow> elist = table.getElementList();
 			if (elist.isEmpty()) {
 				return "列挙定数を入力して下さい。";
 			}
 			return null;
 		}
 
 		return "どちらかを選択して下さい。";
 	}
 
 	public String getEnumName() {
 		if (button0.getSelection()) {
 			return enumCombo.getText().trim();
 		} else {
 			return nameText.getText().trim();
 		}
 	}
 
 	public String getEnumComment() {
 		if (button1.getSelection()) {
 			return commentText.getText().trim();
 		}
		return null;
 	}
 
 	public List<EnumRow> getEnumList() {
 		if (button1.getSelection()) {
 			return table.getElementList();
 		}
 		return null;
 	}
 }
