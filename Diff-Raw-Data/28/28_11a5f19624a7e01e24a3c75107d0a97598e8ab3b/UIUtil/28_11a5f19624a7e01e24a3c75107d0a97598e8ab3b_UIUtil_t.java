 /*===========================================================================
   Copyright (C) 2008-2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.common.ui;
 
 import java.io.IOException;
 import java.net.URL;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.program.Program;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 public class UIUtil {
 
 	public static final int PFTYPE_WIN      = 0;
 	public static final int PFTYPE_MAC      = 1;
 	public static final int PFTYPE_UNIX     = 2;
 
 	/**
 	 * Starts a program or a command.
 	 * @param command Command or program to launch. This can also be a 
 	 * URL string (to open a browser), etc.
 	 */
 	static public void start (String command) {
 		Program.launch(command);
 	}
 
 	/**
 	 * Finds the browser for this system and invoke it with a given URL.
 	 * @param url URL of the resource to open with its associated program (can be a file) 
 	 */
 	static public void start (URL url) {
 		if ( url == null ) return;
 		Program p = Program.findProgram(".html");
 		if ( p!=null ) p.execute(url.toString());
 	}
 	
 	/**
 	 * Executes a given program with a given parameter.
 	 * @param program The program to execute.
 	 * @param parameter The parameter of the program.
 	 */
 	static public void execute (String program,
 		String parameter)
 	{
 		try {
 			Runtime rt = Runtime.getRuntime();
 			String[] command = new String[2];
 			command[0] = program;
 			command[1] = parameter;
 			rt.exec(command);
 		}
 		catch ( IOException e ) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	/**
 	 * Gets the type of platform the application is running on.
 	 * @return -1 if the type could not be detected. Otherwise one of the PFTYPE_* values.
 	 */
 	public static int getPlatformType () {
 		if ( "win32".equals(SWT.getPlatform()) ) return PFTYPE_WIN;
 		if ( "gtk".equals(SWT.getPlatform()) ) return PFTYPE_UNIX;
 		if ( "carbon".equals(SWT.getPlatform()) ) return PFTYPE_MAC;
 		if ( "motif".equals(SWT.getPlatform()) ) return PFTYPE_UNIX;
 		return -1; // Unknown
 	}
 
 	/**
 	 * Creates a new button on a GridLayout layout.
 	 * @param parent Composite parent widget.
 	 * @param style Style of the button.
 	 * @param label Text of the button.
 	 * @param width Width of the button. (Use -1 to not set it)
 	 * @param horizontalSpan Span of the button (Use -1 to not set it)
 	 * @return The new button.
 	 */
 	public static Button createGridButton (Composite parent,
 		int style,
 		String label,
 		int width,
 		int horizontalSpan)
 	{
 		Button newButton = new Button(parent, style);
 		newButton.setText(label);
 		GridData gdTmp = new GridData();
 		if ( width > -1 ) gdTmp.widthHint = width;
 		if ( horizontalSpan > 0 ) gdTmp.horizontalSpan = horizontalSpan;
 		newButton.setLayoutData(gdTmp);
 		return newButton;
 	}
 	
 	/**
 	 * Sets the icon of a dialog to the image(s) inherited from the parent
 	 * shell. If the parent has several images available, they are passed to the
 	 * dialog, otherwise the current result of parent.getImage() is used. 
 	 * @param dialog The dialog where to set the icon.
	 * @param parent The parent to inherit from (if null nothing is done).
 	 */
 	public static void inheritIcon (Shell dialog,
 		Shell parent)
 	{
 		if ( parent == null ) return;
 		Image[] list = parent.getImages();
 		if (( list == null ) || ( list.length < 2 )) dialog.setImage(parent.getImage()); 
 		else dialog.setImages(list);
 	}
 
 	/**
 	 * Converts a string representation of 4 integers into a Rectangle object.
 	 * @param data The string to convert ("X,Y;W;H" or "X,Y,W,H" or "X Y W H").
 	 * @return A new rectangle or null if an error occurred. 
 	 */
 	public static Rectangle StringToRectangle (String data) {
 		try {
 			if ( data == null ) return null;
 			String[] parts = data.split("[,; ]");
 			if ( parts.length != 4 ) return null;
 			int x = Integer.valueOf(parts[0]);
 			int y = Integer.valueOf(parts[1]);
 			int w = Integer.valueOf(parts[2]);
 			int h = Integer.valueOf(parts[3]);
 			return new Rectangle(x, y, w, h);
 		}
 		catch ( Throwable e ) {
 			return null;
 		}
 	}
 
 	/**
 	 * Ensures that a control using GridData layout has a given minimum with.
 	 * @param control the control to adjust.
 	 * @param minimumWidth the minimum width to use.
 	 */
 	public static void ensureWidth (Control control,
 		int minimumWidth)
 	{
 		control.pack();
 		Rectangle rect = control.getBounds();
 		if ( rect.width < minimumWidth ) {
 			((GridData)control.getLayoutData()).widthHint = minimumWidth;
 		}
 	}
 	
 	/**
 	 * Checks the selected path against the project folder variable and set
 	 * the edit field as appropriate.
 	 * @param path the new selected path. 
 	 * @param edField the edit field where to show the path.
 	 * @param projectDir the project folder (can be null).
 	 */
 	public static void checkProjectFolderAfterPick (String path,
 		Text edField,
 		String projectDir)
 	{
 		if ( path == null ) return;
 //TODO: Implement projectDir variable		
 //		String oriPath = edField.getText().replace(BaseUtility.VAR_PROJDIR, projectDir);
 //		if ( !path.equals(oriPath) ) {
 			edField.setText(path);
 //		}
 		edField.selectAll();
 		edField.setFocus();
 	}
 }
