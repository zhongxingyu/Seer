 /* OpenMark online assessment system
    Copyright (C) 2007 The Open University
 
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package om.stdcomponent;
 
 import om.OmDeveloperException;
 import om.stdquestion.QComponentManager;
 
 /** Just a list of standard components. */
 public class ComponentRegistry
 {
 	private static final Class<?>[] COMPONENTCLASSES=
 	{
 		// When adding extra standard components, put them here
 		AdvancedFieldComponent.class,
 		AppletComponent.class,
 		AudioComponent.class,		
 		BoxComponent.class,
 		BreakComponent.class,
 		ButtonComponent.class,
 		CanvasComponent.class,
 		CheckboxComponent.class,
 		CentreComponent.class,
 		DragBoxComponent.class,
 		DropBoxComponent.class,
 		DropdownComponent.class,
 		EditFieldComponent.class,
 		EmphasisComponent.class,
 		EquationComponent.class,
 		FlashComponent.class,
 		GapComponent.class,
 		IfComponent.class,
		IFrameButtonComponent.class,
 		IFrameComponent.class,
 		ImageComponent.class,
 		IndentComponent.class,
 		JMEComponent.class,
 		LabelComponent.class,
 		LayoutGridComponent.class,
 		LinkComponent.class,
 		ListComponent.class,
 		MathMLEquationComponent.class,
 		RadioBoxComponent.class,
 		RightComponent.class,
 		TableComponent.class,
 		TextComponent.class,
 		TextEquationComponent.class,
 		WordSelectComponent.class		
 		// Do not include root component as it can't be created from a tag
 	};
 
 	/**
 	 * Register all known components with a QComponentManager.
 	 * @param qcm the QComponentManager to register components with.
 	 * @throws OmDeveloperException
 	 */
 	public static void fill(QComponentManager qcm) throws OmDeveloperException
 	{
 		for(int i=0;i<COMPONENTCLASSES.length;i++)
 		{
 			qcm.register(COMPONENTCLASSES[i]);
 		}
 	}
 
 }
