 package com.patrick_vane.unrealscript.editor.outline.actions;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jface.action.Action;
 import com.patrick_vane.unrealscript.editor.UnrealScriptEditor;
 import com.patrick_vane.unrealscript.editor.UnrealScriptID;
 import com.patrick_vane.unrealscript.editor.outline.OutlineContentPage;
 
 
 public class ToggleVariablesAction extends Action
 {
 	private final OutlineContentPage page;
 	
 	
 	public ToggleVariablesAction( OutlineContentPage page )
 	{
 		super( "Hide Variables", AS_CHECK_BOX );
 		setImageDescriptor( UnrealScriptEditor.getImageDescriptor("outline/button_hide_variables.gif") );
 		
 		this.page = page;
 		
 		setChecked( areVariablesHidden() );
 	}
 	
 	
 	@Override
 	public void run()
 	{
 		try
 		{
 			UnrealScriptEditor.getRoot().setPersistentProperty( UnrealScriptID.PROPERTY_HIDE_VARIABLES, Boolean.toString(!areVariablesHidden()) );
			page.update( true );
 		}
 		catch( CoreException e )
 		{
 		}
 	}
 	
 	
 	public boolean areVariablesHidden()
 	{
 		try
 		{
 			return Boolean.valueOf( UnrealScriptEditor.getRoot().getPersistentProperty(UnrealScriptID.PROPERTY_HIDE_VARIABLES) );
 		}
 		catch( CoreException e )
 		{
 		}
 		return false;
 	}
 }
