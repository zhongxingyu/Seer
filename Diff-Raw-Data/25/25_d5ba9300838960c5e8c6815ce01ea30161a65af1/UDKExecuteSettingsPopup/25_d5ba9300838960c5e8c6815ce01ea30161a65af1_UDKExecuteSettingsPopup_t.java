 package com.patrick_vane.unrealscript.editor.popups;
 
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.HashMap;
import java.util.Map.Entry;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import com.patrick_vane.unrealscript.editor.UnrealScriptEditor;
 import com.patrick_vane.unrealscript.editor.constants.UnrealScriptID;
 import com.patrick_vane.unrealscript.editor.default_classes.MyArraySorter;
 import com.patrick_vane.unrealscript.editor.default_classes.MySerializer;
 import com.patrick_vane.unrealscript.editor.executable.Profile;
 
 
 public class UDKExecuteSettingsPopup extends ExtendablePopup
 {
 	private static final long		serialVersionUID	= 2238284413436L;
 	
 	private IProject				project;
 	
 	private JComboBox<String>		dropdownProfile;
 	private JComboBox<String>		dropdownMap;
 	private JComboBox<String>		dropdownMode;
 	private JTextField				inputArgs;
 	private JCheckBox				checkboxDisableSound;
 	private JCheckBox				checkboxDisableStartupVideos;
 	private JButton					buttonSave;
 	private JButton					buttonDelete;
 	private JButton					buttonCancel;
 	
 	private HashMap<String,Profile>	profiles;
 	
 	private String 					selectedProfile = "Default";
 	
 	
 	@SuppressWarnings("unchecked")
 	public UDKExecuteSettingsPopup( IProject project )
 	{
 		super( "UDK Execute Settings", 305, 335 );
 		
 		this.project = project;
 		
 		String profilesString;
 		String selectedProfile;
 		String map;
 		String mode;
 		String extraArgs;
 		boolean disableSound;
 		boolean disableStartupVideos;
 		try
 		{
 			profilesString 			= project.getPersistentProperty( UnrealScriptID.PROPERTY_EXECUTE_SETTINGS_PROFILES );
 			selectedProfile 		= project.getPersistentProperty( UnrealScriptID.PROPERTY_SELECTED_EXECUTE_SETTINGS_PROFILE );
 			map 					= project.getPersistentProperty( UnrealScriptID.PROPERTY_GAME_MAP );
 			mode 					= project.getPersistentProperty( UnrealScriptID.PROPERTY_GAME_MODE );
 			extraArgs 				= project.getPersistentProperty( UnrealScriptID.PROPERTY_EXTRA_EXECUTE_ARGUMENTS );
 			disableSound 			= Boolean.parseBoolean( project.getPersistentProperty(UnrealScriptID.PROPERTY_DISABLE_SOUND) );
 			disableStartupVideos 	= Boolean.parseBoolean( project.getPersistentProperty(UnrealScriptID.PROPERTY_DISABLE_STARTUP_VIDEOS) );
 		}
 		catch( CoreException e )
 		{
 			profilesString 			= null;
 			selectedProfile 		= null;
 			map 					= null;
 			mode 					= null;
 			extraArgs 				= null;
 			disableSound 			= Profile.DEFAULT_DISABLE_SOUND;
 			disableStartupVideos 	= Profile.DEFAULT_DISABLE_STARTUP_VIDEOS;
 			
 			JOptionPane.showMessageDialog
 			( 
 				this, 
 				"Failed to load profiles: "+e.getMessage()+"\nYour profiles have been reset.", 
 				"Warning",
 				JOptionPane.WARNING_MESSAGE
 			);
 		}
 		if( selectedProfile == null )
 			selectedProfile = "Default";
 		if( map == null )
 			map = Profile.DEFAULT_MAP;
 		if( mode == null )
 			mode = Profile.DEFAULT_MODE;
 		if( extraArgs == null )
 			extraArgs = Profile.DEFAULT_EXTRA_ARGS;
 		
 		this.selectedProfile = selectedProfile;
 		
 		try
 		{
 			profiles = (HashMap<String,Profile>) MySerializer.fromString( profilesString );
 		}
 		catch( Exception e )
 		{
 			profiles = null;
 		}
 		if( (profiles == null) || profiles.isEmpty() )
 		{
 			profiles = new HashMap<String,Profile>();
 			profiles.put( "Default", new Profile() );
 		}
 		
 		
 		JLabel labelProfile = new JLabel( "Profile:" );
 		labelProfile.setBounds( 23, 27, 48, 14 );
 		getContentPane().add( labelProfile );
 		
 		dropdownProfile = new JComboBox<String>();
 		dropdownProfile.setModel( new DefaultComboBoxModel<String>(MyArraySorter.sort(profiles.keySet().toArray(new String[0]))) );
 		dropdownProfile.setEditable( true );
 		dropdownProfile.setToolTipText( "Settings profile" );
 		dropdownProfile.setBounds( 86, 25, 198, 20 );
 		dropdownProfile.setSelectedItem( selectedProfile );
 		dropdownProfile.addActionListener( listenerChangeProfile );
 		getContentPane().add( dropdownProfile );
 		
 		
 		JLabel labelMap = new JLabel( "Map:" );
 		labelMap.setBounds( 23, 72, 38, 14 );
 		getContentPane().add( labelMap );
 		
 		dropdownMap = new JComboBox<String>();
 		dropdownMap.setModel( new DefaultComboBoxModel<String>(UnrealScriptEditor.getMapNames(project)) );
 		dropdownMap.setEditable( true );
 		dropdownMap.setToolTipText( "A map from the UDKGame/Content/Maps/ folder" );
 		dropdownMap.setBounds( 67, 70, 217, 20 );
 		dropdownMap.setSelectedItem( map );
 		dropdownMap.setFont( new Font(dropdownMap.getFont().getFontName(), dropdownMap.getFont().getStyle(), (int) Math.round(dropdownMap.getFont().getSize()*0.75)) );
 		getContentPane().add( dropdownMap );
 		
 		
 		JLabel labelMode = new JLabel( "Mode:" );
 		labelMode.setBounds( 23, 102, 36, 14 );
 		getContentPane().add( labelMode );
 		
 		dropdownMode = new JComboBox<String>();
 		dropdownMode.setModel( new DefaultComboBoxModel<String>(UnrealScriptEditor.getClassesAndPackageNames(UnrealScriptEditor.getSubClasses("GameInfo"))) );
 		dropdownMode.setToolTipText( "A game mode" );
 		dropdownMode.setEditable( true );
 		dropdownMode.setBounds( 67, 100, 217, 20 );
 		dropdownMode.setSelectedItem( mode );
 		dropdownMode.setFont( new Font(dropdownMode.getFont().getFontName(), dropdownMode.getFont().getStyle(), (int) Math.round(dropdownMode.getFont().getSize()*0.75)) );
 		getContentPane().add( dropdownMode );
 		
 		
 		JLabel labelArgs = new JLabel( "Args:" );
 		labelArgs.setBounds( 23, 133, 36, 14 );
 		getContentPane().add( labelArgs );
 		
 		inputArgs = new JTextField();
 		inputArgs.setToolTipText( "Extra arguments" );
 		inputArgs.setEditable( true );
 		inputArgs.setBounds( 67, 131, 217, 20 );
 		inputArgs.setText( extraArgs );
 		getContentPane().add( inputArgs );
 		
 		
 		checkboxDisableSound = new JCheckBox( "disable sound" );
 		checkboxDisableSound.setToolTipText( "Disables the ingame sound" );
 		checkboxDisableSound.setBounds( 86, 166, 114, 21 );
 		checkboxDisableSound.setSelected( disableSound );
 		getContentPane().add( checkboxDisableSound );
 		
 		checkboxDisableStartupVideos = new JCheckBox( "disable startup videos" );
 		checkboxDisableStartupVideos.setToolTipText( "Disables the startup videos" );
 		checkboxDisableStartupVideos.setBounds( 86, 191, 156, 21 );
 		checkboxDisableStartupVideos.setSelected( disableStartupVideos );
 		getContentPane().add( checkboxDisableStartupVideos );
 		
 		
 		buttonSave = new JButton( "Save" );
 		buttonSave.setToolTipText( "Save and close" );
 		buttonSave.setBounds( 112, 233, 89, 23 );
 		buttonSave.addActionListener( listenerSave );
 		getContentPane().add( buttonSave );
 		
 		buttonDelete = new JButton( "Delete" );
 		buttonDelete.setToolTipText( "Delete selected profile" );
 		buttonDelete.setBounds( 112, 262, 89, 23 );
 		buttonDelete.addActionListener( listenerDelete );
 		getContentPane().add( buttonDelete );
 		
 		buttonCancel = new JButton( "Cancel" );
 		buttonCancel.setToolTipText( "Close without saving" );
 		buttonCancel.setBounds( 112, 291, 89, 23 );
 		buttonCancel.addActionListener( listenerCancel );
 		getContentPane().add( buttonCancel );
 	}
 	
 	
 	private Profile saveCurrentProfile()
 	{
 		return saveProfile( (String) dropdownProfile.getSelectedItem() );
 	}
 	private Profile saveProfile( String profileName )
 	{
 		Profile profile 				= new Profile();
 		profile.map 					= (String) dropdownMap.getSelectedItem();
 		profile.mode 					= (String) dropdownMode.getSelectedItem();
 		profile.extraArgs 				= inputArgs.getText();
 		profile.disableSound 			= checkboxDisableSound.isSelected();
 		profile.disableStartupVideos 	= checkboxDisableStartupVideos.isSelected();
 		return saveProfile( profile, profileName );
 	}
 	private Profile saveProfile( Profile profile, String profileName )
 	{
 		profiles.put( profileName, profile );
		clearNullProfiles();
 		
 		String selectedProfile = (String) dropdownProfile.getSelectedItem();
 		
 		dropdownProfile.removeActionListener( listenerChangeProfile );
 			dropdownProfile.removeAllItems();
 			for( String key : MyArraySorter.sort(profiles).keySet() )
 			{
 				dropdownProfile.addItem( key );
 			}
 			dropdownProfile.setSelectedItem( selectedProfile );
 		dropdownProfile.addActionListener( listenerChangeProfile );
 		
 		return profile;
 	}
 	
 	private Profile loadProfile( String profileName )
 	{
 		Profile profile = profiles.get( profileName );
 		if( profile == null )
 			profile = new Profile();
 		
 		dropdownProfile.removeActionListener( listenerChangeProfile );
 		dropdownProfile.setSelectedItem( profileName );
 		dropdownProfile.addActionListener( listenerChangeProfile );
 		
 		dropdownMap.setSelectedItem( profile.map );
 		dropdownMode.setSelectedItem( profile.mode );
 		inputArgs.setText( profile.extraArgs );
 		checkboxDisableSound.setSelected( profile.disableSound );
 		checkboxDisableStartupVideos.setSelected( profile.disableStartupVideos );
 		
 		return profile;
 	}
 	
 	
 	private boolean save()
 	{
 		try
 		{
 			Profile profile = saveCurrentProfile();
 			project.setPersistentProperty( UnrealScriptID.PROPERTY_EXECUTE_SETTINGS_PROFILES, MySerializer.toString(profiles) );
 			project.setPersistentProperty( UnrealScriptID.PROPERTY_SELECTED_EXECUTE_SETTINGS_PROFILE, (String) dropdownProfile.getSelectedItem() );
 			project.setPersistentProperty( UnrealScriptID.PROPERTY_GAME_MAP, profile.map );
 			project.setPersistentProperty( UnrealScriptID.PROPERTY_GAME_MODE, profile.mode );
 			project.setPersistentProperty( UnrealScriptID.PROPERTY_EXTRA_EXECUTE_ARGUMENTS, profile.extraArgs );
 			project.setPersistentProperty( UnrealScriptID.PROPERTY_DISABLE_SOUND, Boolean.toString(profile.disableSound) );
 			project.setPersistentProperty( UnrealScriptID.PROPERTY_DISABLE_STARTUP_VIDEOS, Boolean.toString(profile.disableStartupVideos) );
 			return true;
 		}
 		catch( Exception e )
 		{
			e.printStackTrace();
 			JOptionPane.showMessageDialog
 			( 
 				this, 
 				"Failed to save profiles: "+e.getMessage(), 
 				"Error",
 				JOptionPane.ERROR_MESSAGE
 			);
 			return false;
 		}
 	}
 	
 	
 	private ActionListener listenerSave = new ActionListener()
 	{
 		@Override
 		public void actionPerformed( ActionEvent e )
 		{
 			if( save() )
 			{
 				success();
 			}
 		}
 	};
 	
 	private ActionListener listenerDelete = new ActionListener()
 	{
 		@Override
 		public void actionPerformed( ActionEvent e )
 		{
 			try
 			{
 				dropdownProfile.removeActionListener( listenerChangeProfile );
 					int index = dropdownProfile.getSelectedIndex();
 					profiles.remove( dropdownProfile.getSelectedItem() );
 					dropdownProfile.removeItemAt( index );
 					
 					dropdownProfile.setSelectedIndex( Math.max(0, Math.min(profiles.size()-1, index)) );
 					selectedProfile = (String) dropdownProfile.getSelectedItem();
 				dropdownProfile.addActionListener( listenerChangeProfile );
				
				loadProfile( selectedProfile );
 			}
 			catch( Exception ex )
 			{
 			}
 		}
 	};
 	
 	private ActionListener listenerCancel = new ActionListener()
 	{
 		@Override
 		public void actionPerformed( ActionEvent e )
 		{
 			cancel();
 		}
 	};
 	
 	
 	private ActionListener listenerChangeProfile = new ActionListener()
 	{
 		@Override
 		public void actionPerformed( ActionEvent e )
 		{
 			if( "comboBoxChanged".equals(e.getActionCommand()) )
 			{
 				if( selectedProfile == null )
 					selectedProfile = "Default";
 				
 				Profile profile = saveProfile( selectedProfile );
 				
 				selectedProfile = (String) dropdownProfile.getSelectedItem();
 				if( !profiles.containsKey(selectedProfile) )
 				{
 					saveProfile( profile, selectedProfile );
 				}
 				
 				loadProfile( selectedProfile );
 			}
 		}
 	};
	
	
	private void clearNullProfiles()
	{
		HashMap<String,Profile> newArray = new HashMap<String,Profile>();
		for( Entry<String,Profile> entry : profiles.entrySet() )
		{
			if( entry.getValue() != null )
			{
				newArray.put( entry.getKey(), entry.getValue() );
			}
		}
		profiles = newArray;
	}
 }
