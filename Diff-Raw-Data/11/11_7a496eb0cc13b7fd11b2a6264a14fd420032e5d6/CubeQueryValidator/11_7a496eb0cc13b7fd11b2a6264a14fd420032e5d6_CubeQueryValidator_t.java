 /*******************************************************************************
  * Copyright (c) 2004, 2005 Actuate Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *  Actuate Corporation  - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.birt.data.engine.olap.query.view;
 
 import org.eclipse.birt.data.engine.core.DataException;
 import org.eclipse.birt.data.engine.i18n.ResourceConstants;
 import org.eclipse.birt.data.engine.olap.api.cube.ICube;
 import org.eclipse.birt.data.engine.olap.api.query.ILevelDefinition;
 
 /**
  * validate cube query defintion with edge, measure definition.
  * 
  */
 class CubeQueryValidator
 {
 
 	private CubeQueryValidator( )
 	{
 	}
 
 	/**
 	 * 
 	 * @param view
 	 * @param cube
 	 * @param calculatedMember
 	 * @throws DataException
 	 */
 	static void validateCubeQueryDefinition( BirtCubeView view, ICube cube,
 			CalculatedMember[] calculatedMember ) throws DataException
 	{
		if ( view.getColumnEdgeView( ) == null
				&& view.getRowEdgeView( ) == null )
		{
			throw new DataException( ResourceConstants.NO_EDGEDEFN_FOUND );
		}
 		if ( view.getColumnEdgeView( ) != null )
 		{
 			validateOnEdgeDefinition( cube, view.getColumnEdgeView( ) );
 		}
 		if ( view.getRowEdgeView( ) != null )
 		{
 			validateOnEdgeDefinition( cube, view.getRowEdgeView( ) );
 		}
 		if ( calculatedMember != null && calculatedMember.length > 0 )
 		{
 			validateCalculatedMember( cube, calculatedMember );
 		}
 	}
 
 	/**
 	 * validate on calculated member to verify whether the measure reference
 	 * exist.
 	 * 
 	 * @param cube
 	 * @param measureDefn
 	 * @throws DataException
 	 */
 	static void validateCalculatedMember( ICube cube,
 			CalculatedMember[] calculatedMember ) throws DataException
 	{
 		boolean findMeasure = false;
 		for ( int i = 0; i < calculatedMember.length; i++ )
 		{
 			findMeasure = false;
 			String measureName = calculatedMember[i].getMeasureName( );
 			String[] names = cube.getAllMeasureNames( );
 			if ( names != null && names.length > 0 )
 			{
 				for ( int k = 0; k < names.length; k++ )
 				{
 					if ( names[k].equals( measureName ) )
 					{
 						findMeasure = true;
 						continue;
 					}
 				}
 			}
 			if ( !findMeasure )
 			{
 				throw new DataException( ResourceConstants.MEASURE_NAME_NOT_FOUND,
 						new Object[]{
 							measureName
 						} );
 			}
 
 		}
 	}
 
 	/**
 	 * validate on edge definition
 	 * 
 	 * @param cube
 	 * @param edgeView
 	 * @throws DataException
 	 */
 	static void validateOnEdgeDefinition( ICube cube, BirtEdgeView edgeView )
 			throws DataException
 	{
 		for ( int i = 0; i < edgeView.getDimensionViews( ).size( ); i++ )
 		{
 			BirtDimensionView birtDimensionView = (BirtDimensionView) edgeView.getDimensionViews( )
 					.get( i );
 			for ( int j = 0; j < birtDimensionView.getMemberSelection( ).size( ); j++ )
 			{
 				ILevelDefinition levelDefinition = (ILevelDefinition) birtDimensionView.getMemberSelection( )
 						.get( j );
 				String dimensionName = levelDefinition.getHierarchy( )
 						.getDimension( )
 						.getName( );
 				String levelName = levelDefinition.getName( );
 				String hierarchyName = levelDefinition.getHierarchy( )
 						.getName( );
 				if ( !validateWithRawCube( cube,
 						levelName,
 						dimensionName,
 						hierarchyName ) )
 				{
 					throw new DataException( ResourceConstants.CANNOT_FIND_LEVEL,
 							new Object[]{
 									levelName, hierarchyName, dimensionName
 							} );
 				}
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * @param cube
 	 * @param dimensionName
 	 * @param levelName
 	 * @param hierarchyName
 	 * @return
 	 */
 	static boolean validateWithRawCube( ICube cube, String levelName,
 			String dimensionName, String hierarchyName )
 	{
 		boolean validate = false;
 		for ( int k = 0; k < cube.getDimesions( ).length; k++ )
 		{
 			if ( dimensionName.equals( cube.getDimesions( )[k].getName( ) ) )
 			{
 				if ( hierarchyName.equals( cube.getDimesions( )[k].getHierarchy( )
 						.getName( ) ) )
 				{
 					for ( int t = 0; t < cube.getDimesions( )[k].getHierarchy( )
 							.getLevels( ).length; t++ )
 					{
 						if ( levelName.equals( cube.getDimesions( )[k].getHierarchy( )
 								.getLevels( )[t].getName( ) ) )
 						{
 							validate = true;
 							return validate;
 						}
 					}
 				}
 			}
 		}
 		return validate;
 	}
 }
