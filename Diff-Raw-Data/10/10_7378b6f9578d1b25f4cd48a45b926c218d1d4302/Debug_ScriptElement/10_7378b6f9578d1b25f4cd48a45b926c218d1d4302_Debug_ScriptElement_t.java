 package com.dafrito.rfe;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.Stack;
 import java.util.Vector;
 
 import javax.swing.BorderFactory;
 import javax.swing.JFileChooser;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTextArea;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.UndoableEditEvent;
 import javax.swing.event.UndoableEditListener;
 import javax.swing.undo.CompoundEdit;
 
 
 public class Debug_ScriptElement extends JPanel implements UndoableEditListener, ListSelectionListener, ComponentListener, MouseListener {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -1513897604566683983L;
 	private File file;
 	private boolean hasChanged, isValid;
 	private static int fileNumber = 1;
 	private DebugEnvironment debugger;
 	private JTextArea textArea;
 	private Stack<CompoundEdit> edits = new Stack<CompoundEdit>();
 	private Stack<CompoundEdit> undoneEdits = new Stack<CompoundEdit>();
 	private long lastEdit;
 	private JList errors;
 	private JSplitPane splitPane;
 	private int width;
 	private Vector<Exception> exceptions;
 	private Vector<String> displayedExceptions;
 	private String prefix = "";
 
 	public Debug_ScriptElement(DebugEnvironment debugger) {
 		this.debugger = debugger;
 		if (this.selectFile(false)) {
 			this.isValid = this.openFile();
 		}
 	}
 
 	public Debug_ScriptElement(DebugEnvironment debugger, File file) {
 		this.debugger = debugger;
 		this.isValid = this.openFile(file);
 	}
 
 	public Debug_ScriptElement(DebugEnvironment debugger, String string) {
 		this.debugger = debugger;
 		if (string != null) {
 			this.file = new File(string);
 		}
 		this.isValid = this.openFile();
 	}
 
 	public void addException(Exception exception) {
 		this.exceptions.add(exception);
 		this.prefix = "X ";
 		this.errors.setListData(this.displayedExceptions);
 		this.errors.setBorder(BorderFactory.createTitledBorder(this.getFilename() + "(" + this.exceptions.size() + " error(s))"));
 		if (exception instanceof Exception_Nodeable) {
 			this.displayedExceptions.add(((Exception_Nodeable) exception).getName());
 		} else {
 			this.displayedExceptions.add(exception.getMessage());
 		}
 	}
 
 	public boolean canRedo() {
 		return this.undoneEdits.size() > 0;
 	}
 
 	public boolean canUndo() {
 		return this.edits.size() > 0;
 	}
 
 	public boolean closeFile() {
 		this.debugger.showReferenced(this);
 		if (!this.hasChanged()) {
 			return true;
 		}
 		int option = JOptionPane.showConfirmDialog(this.debugger, "This file has unsaved changes. Save?", this.getName(), JOptionPane.YES_NO_CANCEL_OPTION);
 		if (option == JOptionPane.NO_OPTION) {
 			return true;
 		}
 		if (option == JOptionPane.CANCEL_OPTION) {
 			return false;
 		}
 		return this.saveFile();
 	}
 
 	public boolean compile() {
 		String text = this.textArea.getText();
 		String[] stringArray = text.split("\n");
 		java.util.List<Object> strings = new LinkedList<Object>();
 		for (int i = 0; i < stringArray.length; i++) {
 			strings.add(new ScriptLine(this.debugger.getEnvironment(), this.getFilename(), i + 1, stringArray[i]));
 		}
 		this.width = this.getWidth();
 		this.splitPane.setRightComponent(new JScrollPane(this.errors = new JList()));
 		this.splitPane.setDividerLocation(this.getWidth() - 200);
 		this.errors.addListSelectionListener(this);
 		this.errors.addMouseListener(this);
 		this.exceptions = Parser.preparseFile(this.debugger.getEnvironment(), this.getFilename(), strings);
 		this.displayedExceptions = new Vector<String>();
 		if (this.exceptions.size() == 0) {
 			this.errors.setBorder(BorderFactory.createTitledBorder("Compiled Successfully"));
 			this.prefix = "";
 			return true;
 		} else {
 			for (Exception ex : this.exceptions) {
 				if (ex instanceof Exception_Nodeable) {
 					this.displayedExceptions.add(((Exception_Nodeable) ex).getName());
 				} else if (ex instanceof Exception_InternalError) {
 					this.displayedExceptions.add(((Exception_InternalError) ex).getName());
 				} else {
 					this.displayedExceptions.add(ex.getMessage());
 				}
 			}
 			this.errors.setListData(this.displayedExceptions);
 			this.errors.setBorder(BorderFactory.createTitledBorder(this.getName() + "(" + this.exceptions.size() + " error(s))"));
 			this.prefix = "X ";
 			return false;
 		}
 	}
 
 	@Override
 	public void componentHidden(ComponentEvent x) {
 	}
 
 	@Override
 	public void componentMoved(ComponentEvent x) {
 	}
 
 	@Override
 	public void componentResized(ComponentEvent x) {
 		double location = ((double) this.splitPane.getDividerLocation()) / (double) this.width;
 		if (location > 1) {
 			location = 1;
 		}
 		this.splitPane.setDividerLocation((int) (this.getWidth() * location));
 		this.width = this.getWidth();
 	}
 
 	@Override
 	public void componentShown(ComponentEvent x) {
 	}
 
 	public String getFilename() {
 		if (this.file != null) {
 			return this.file.getName();
 		}
 		return "Untitled " + fileNumber++;
 	}
 
 	@Override
 	public String getName() {
 		return this.prefix + this.getFilename();
 	}
 
 	public boolean hasChanged() {
 		return this.hasChanged;
 	}
 
	public boolean hasFile() {
 		return this.isValid;
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		if (e.getClickCount() != 2) {
 			return;
 		}
 		if (this.errors.getSelectedValue() == null) {
 			return;
 		}
 		Exception rawEx = this.exceptions.get(this.errors.getSelectedIndex());
 		if (!(rawEx instanceof Exception_Nodeable)) {
 			return;
 		}
 		Exception_Nodeable ex = (Exception_Nodeable) rawEx;
 		String[] array = this.textArea.getText().split("\n");
 		int offset = 0;
 		this.textArea.requestFocus();
 		for (int i = 0; i < ex.getLineNumber(); i++) {
 			if (array.length - 1 == i) {
 				this.textArea.setCaretPosition(offset);
 				return;
 			}
 			offset += array[i].length() + 1;
 			if (i == ex.getLineNumber() - 1) {
 				i++;
 				if (array[i].length() < ex.getOffset()) {
 					this.textArea.setCaretPosition(offset + array[i].length());
 					return;
 				}
 				if (array[i].substring(ex.getOffset()).length() < (ex.getLength() - ex.getOffset())) {
 					this.textArea.setCaretPosition(offset + ex.getOffset());
 					this.textArea.select(offset + ex.getOffset(), offset + array[i].length() - ex.getOffset());
 					return;
 				}
 				break;
 			}
 		}
 		this.textArea.select(offset + ex.getOffset() + 1, 1 + offset + ex.getOffset() + ex.getLength());
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 	}
 
 	public boolean openFile() {
 		return this.openFile(this.file);
 	}
 
 	public boolean openFile(File file) {
 		this.setLayout(new GridLayout(0, 1));
 		this.splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
 		this.splitPane.add(new JScrollPane(this.textArea = new JTextArea()));
 		this.add(this.splitPane);
 		this.textArea.setFont(new Font("Courier", Font.PLAIN, 12));
 		if (file != null) {
 			this.file = file;
 			if (!this.readFile()) {
 				return false;
 			}
 		}
 		this.textArea.getDocument().addUndoableEditListener(this);
 		this.addComponentListener(this);
 		this.setVisible(true);
 		return true;
 	}
 
 	@Override
 	public void paint(Graphics g) {
 		super.paint(g);
 	}
 
 	public boolean readFile() {
 		return this.readFile(this.file);
 	}
 
 	public boolean readFile(File file) {
 		try {
 			BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
 			byte[] b = new byte[in.available()];
 			in.read(b, 0, b.length);
 			this.textArea.setText(new String(b, 0, b.length));
 			in.close();
 			return true;
 		} catch (IOException ex) {
 			JOptionPane.showMessageDialog(this, "File failed to open: " + file.getName(), "IOException", JOptionPane.ERROR_MESSAGE);
 		}
 		return false;
 	}
 
 	public void redo() {
 		this.setChanged(true);
 		if (this.undoneEdits.size() > 0) {
 			CompoundEdit edit = this.undoneEdits.pop();
 			edit.redo();
 			this.edits.push(edit);
 			if (this.undoneEdits.size() == 0) {
 				this.debugger.setCanRedo(false);
 			}
 			this.debugger.setCanUndo(true);
 		}
 	}
 
 	public boolean saveFile() {
 		if (!this.hasChanged()) {
 			return true;
 		}
 		if (this.file == null) {
 			return this.saveFileAs();
 		}
 		return this.writeFile();
 	}
 
 	public boolean saveFileAs() {
 		this.debugger.showReferenced(this);
 		if (!this.selectFile(true)) {
 			return false;
 		}
 		if (!this.writeFile()) {
 			return false;
 		}
 		this.debugger.resetTitle(this);
 		return true;
 	}
 
 	public boolean selectFile(boolean isSaving) {
 		JFileChooser fileChooser = new JFileChooser(".");
 		ExtensionFilter filter = new ExtensionFilter("RiffScripts");
 		filter.addExtension("RiffScript");
 		fileChooser.setFileFilter(filter);
 		int choice = 0;
 		if (isSaving) {
 			choice = fileChooser.showSaveDialog(this);
 		} else {
 			choice = fileChooser.showOpenDialog(this);
 		}
 		if (choice == JFileChooser.APPROVE_OPTION) {
 			if (fileChooser.getSelectedFile().getName().indexOf(".") == -1) {
 				this.file = new File(fileChooser.getSelectedFile().getName() + ".RiffScript");
 			} else {
 				this.file = fileChooser.getSelectedFile();
 			}
 			return true;
 		}
 		return false;
 	}
 
 	public void setChanged(boolean changed) {
 		this.hasChanged = changed;
 		if (changed) {
 			this.prefix = "* ";
 		} else {
 			this.prefix = "";
 		}
 		this.debugger.setChanged(changed);
 	}
 
 	public void setPrefix(String prefix) {
 		this.prefix = prefix;
 	}
 
 	public void undo() {
 		this.setChanged(true);
 		this.edits.peek().end();
 		if (this.edits.size() > 0) {
 			CompoundEdit edit = this.edits.pop();
 			if (edit.canUndo()) {
 				edit.undo();
 			}
 			this.undoneEdits.push(edit);
 			if (this.edits.size() == 0) {
 				this.debugger.setCanUndo(false);
 			}
 			this.debugger.setCanRedo(true);
 		}
 	}
 
 	@Override
 	public void undoableEditHappened(UndoableEditEvent e) {
 		if (this.edits.size() > 0) {
 			if (System.currentTimeMillis() - this.lastEdit > 1000) {
 				this.edits.peek().end();
 			} else {
 				this.edits.peek().addEdit(e.getEdit());
 				return;
 			}
 		}
 		if (e.getEdit().isSignificant()) {
 			this.setChanged(true);
 			this.debugger.setCanUndo(true);
 			CompoundEdit edit = new CompoundEdit();
 			edit.addEdit(e.getEdit());
 			this.edits.add(edit);
 			this.undoneEdits.clear();
 		}
 		this.lastEdit = System.currentTimeMillis();
 	}
 
 	@Override
 	public void valueChanged(ListSelectionEvent e) {
 
 	}
 
 	public boolean writeFile() {
 		return this.writeFile(this.file);
 	}
 
 	public boolean writeFile(File file) {
 		try {
 			FileWriter writer = new FileWriter(file);
 			writer.write(this.textArea.getText(), 0, this.textArea.getText().length());
 			writer.close();
 			this.setChanged(false);
 			return true;
 		} catch (IOException ex) {
 			JOptionPane.showMessageDialog(this, "File failed to save: " + file.getName(), "IOException", JOptionPane.ERROR_MESSAGE);
 		}
 		return false;
 	}
 }
