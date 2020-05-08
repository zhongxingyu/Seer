 /*
**  $Id: GenerateRadar.java,v 1.1 2006/06/16 14:16:35 goneri Exp $
 **
 **  Copyright (C) 2006 ESME SUDRIA ( www.esme.fr ) 
 **
 **  Authors: 
 **	BOUCHER Nicolas <shoub_n@hotmail.com>
 **  	MODELIN Maxence  <maxence_modelin@hotmail.com>
 **  	MULOT Louis <vindic@noos.fr>
 **
 **  This program is free software; you can redistribute it and/or modify
 ** it under the terms of the GNU General Public License as published by
 **  the Free Software Foundation; either version 2 of the License, or
 **  (at your option) any later version.
 **
 ** This program is distributed in the hope that it will be useful,
 **  but WITHOUT ANY WARRANTY; without even the implied warranty of
 **  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 **  GNU General Public License for more details.
 **
 **  You should have received a copy of the GNU General Public License
 ** along with this program; if not, write to the Free Software
 ** Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 **
 **
 */
 package org.qsos.radar;
 
 
 
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.CheckStateChangedEvent;
 import org.eclipse.jface.viewers.CheckboxTreeViewer;
 import org.eclipse.jface.viewers.ICheckStateListener;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.awt.SWT_AWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.plot.SpiderWebPlot;
 import org.jfree.data.category.DefaultCategoryDataset;
 import org.qsos.data.IElement;
 import org.qsos.data.JQConst;
 import org.qsos.data.Messages;
 import org.qsos.interfaces.SheetCTabItem;
 import org.qsos.main.JQ;
 
 
 
 public class GenerateRadar
 {
 	
 	static JQ window;
 	public Object[] categories=new Object[7] ;
 	public int numCat;
 	public String TextCat[]=new String[7];
 	public double ScoreCat[]=new double[7];
 	public Double ScoreCat2[]=new Double[20];
 	public Double ScoreCat3[]=new Double[20];
 	public Double ScoreCat4[]=new Double[20];
 	public Double ScoreCat5[]=new Double[20];
 	public double SumScore;
 	public double SumScore2;
 	public double SumScore3;
 	public double SumScore4;
 	public static int CatNumber;
 	
 	/**
 	 * Contructor: 
 	 * 
 	 * @param w
 	 * 					JQ
 	 */
 	
 	public GenerateRadar(JQ w)
 	{
 		window = w;
 	}
 	
 	
 	/**
 	 * This class creates a composite in which the radar char will be
 	 * implemented
 	 * 
 	 * @param parent
 	 * 				Composite where the chart will be seen
 	 * @param Categories
 	 * 				String[] that contains the name of the elements [4 min and 7 max] the user has chosen to visualize
 	 * @param Scores
 	 * 				Double[] that contains the average score of each category
 	 * @return Control
 	 * 
 	 */
 	public static Control createChart(Composite parent,String[] Categories, double[] Scores)
 	{
 		
 		Composite Charcomposite = new Composite(parent, SWT.EMBEDDED);
 		Charcomposite.setLayout(new FillLayout());
 		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
 		
 		//
 		for (int i=0;i<CatNumber;i++)
 		{	
 			dataset.addValue(Scores[i],getTitle(),Categories[i]);
 		}
 		
 		
 		String BackGroundMire=null;
 		
 		//Configuration of the spiderwebplot
 		SpiderWebPlot plot = new SpiderWebPlot();
 		plot.setDataset(dataset);	
 		plot.setMaxValue(JQConst.RADAR_MAX_VALUE);
 		plot.setSeriesPaint(JQConst.RADAR_SERIES_PAINT);
 		plot.setAxisLabelGap(JQConst.RADAR_AXIS_LABEL_GAP);
 		plot.setHeadPercent(JQConst.RADAR_HEAD_PERCENT);
 		plot.setInteriorGap(JQConst.RADAR_INTERIOR_GAP);
 		plot.setWebFilled(true);
 		
 		//The backgroundpicture used as a spiderweb is chosen according to the 
 		//number of categories selected
 		switch (CatNumber)
 		{
 			case 4: 
 				BackGroundMire=JQConst.RADAR_SPIDERWEB_4;
 				break;
 			case 5: 
 				BackGroundMire=JQConst.RADAR_SPIDERWEB_5;
 				break;
 			case 6: 
 				BackGroundMire=JQConst.RADAR_SPIDERWEB_6;
 				break;
 			case 7: 
 				BackGroundMire=JQConst.RADAR_SPIDERWEB_7;
 				break;
 		}	
 		javax.swing.ImageIcon icon = new javax.swing.ImageIcon(BackGroundMire);
 		plot.setBackgroundImage(icon.getImage());
 		
 		
 		//chart creation
 		JFreeChart chart = new JFreeChart(plot);
 		
 		//Here the background color from the shell is taken in order to match AWT and SWT backgrounds
 		Color backgroundColor = parent.getBackground();
 		
 		//JFreechart doesn't support SWT so we create an AWT Frame that will depend on Charcomposite
 		java.awt.Frame chartPanel = SWT_AWT.new_Frame(Charcomposite); 
 		chartPanel.setLayout(new java.awt.GridLayout());
 		ChartPanel jfreeChartPanel = new ChartPanel(chart);
 		
 		chartPanel.setBackground(new java.awt.Color(backgroundColor.getRed(),backgroundColor.getGreen(),backgroundColor.getBlue())); 
 		
 		chartPanel.add(jfreeChartPanel);
 		chartPanel.pack();
 		
 		
 		return parent;
 		
 	}
 	
 	/**
 	 *This Class creates a shell with a layout and calls the createchart method 
 	 *which will return the radar chart
 	 *
 	 *@param Categories
 	 *					String[] that contains the name of the elements [4 min and 7 max] the user has chosen to visualize
 	 *@param Scores
 	 * 				Double[] that contains the average score of each category
 	 *
 	 *
 	 */
	public void run(String[] Categories, double[] Scores)
 	{
 		if ( window.getCTabFolder().getItemCount() != 0)
 		{			
 			Shell shell = new Shell(window.getShell().getDisplay(),SWT.DIALOG_TRIM);
 			shell.setSize(500,500);
 			FillLayout layout = new FillLayout();
 			layout.type = SWT.VERTICAL;
 			shell.setLayout(layout);
 			
 			shell.forceActive();
 			
 			createChart(shell,Categories,Scores);
 			
 			shell.open();
 			
 		}
 	}
 	
 	/**
 	 * This class creates a composite which include a checkboxtreeviewer and a button.
 	 *  It uses 2 listeners: 1 for the checking if the user has checked a box and 
 	 * another for the 0K button in order to check the validity of the selected elements.
 	 * @param parent
 	 * 				Composite
 	 * @return Control
 	 */
 	
 	public Control createCheckboxTree(Composite parent)
 	{
 		
 		numCat=0;
 		
 		Composite checkTreeComposite = new Composite(parent,SWT.NONE);
 		checkTreeComposite.setLayout(new org.eclipse.swt.layout.FormLayout());
 		
 
 		FormData CheckData = new FormData();
 		final CheckboxTreeViewer checkboxTreeViewer = new CheckboxTreeViewer(checkTreeComposite);
 		checkboxTreeViewer.getTree().setLayoutData(CheckData);
 		CheckData.height=430;
 		CheckData.width=320;
 		
 		checkboxTreeViewer.setContentProvider(new SheetCheckContentProvider(window.getCTabFolder()));
 		checkboxTreeViewer.setLabelProvider(new SheetCheckLabelProvider());
 		checkboxTreeViewer.addFilter(new MyFilter());
 		
 		IElement elementToInput = (((SheetCTabItem) window.getCTabFolder().getSelection()).getLibQSOS().getSheet().getRoot());
 		checkboxTreeViewer.setInput(elementToInput);
 		checkboxTreeViewer.expandToLevel(2);
 		
 		checkboxTreeViewer.addCheckStateListener(new ICheckStateListener()
 		{
 			//This listeners verifies if the user a checked a box and wether it should be appeared checked or not
 			
 			public void checkStateChanged(CheckStateChangedEvent event) {
 				if (event.getChecked()) 	
 				{
 					if (numCat < 7)
 					{
 						categories[numCat++]=event.getElement();
 						checkboxTreeViewer.setSubtreeChecked(event.getElement(),true);
 					}
 					else
 					{
 						//System.out.println("Erreur"); //$NON-NLS-1$
 						checkboxTreeViewer.setChecked(event.getElement(),false);
 					}	
 				}
 				else if (!event.getChecked())
 				{
 					if (((IElement)event.getElement()).getContainer() != null)
 					{
 						boolean state = checkboxTreeViewer.getChecked(((IElement)event.getElement()).getContainer()) ;
 						if (!state)
 						{
 							checkboxTreeViewer.setSubtreeChecked(event.getElement(),false);
 							numCat--;	
 						}
 						else
 						{
 							checkboxTreeViewer.setChecked(event.getElement(),true);
 						}
 					}	
 				}
 			}
 		});
 		
 
 		checkTreeComposite.pack();
 		
 		
 		return checkTreeComposite;
 	}
 	
 	
 	
 	public void createRadar()
 	{
 		int i;
 		int j;
 		int k;
 		int l;
 		int m;
 		CatNumber=0;
 		for (i=0;i<categories.length;i++)
 		{
 			if (categories[i] != null)
 			{
 				CatNumber++;
 			}
 				
 		}	
 		if (CatNumber <= 7 && CatNumber >=4) 
 		{				
 			for(i = 0;i<CatNumber;i++)
 			{
 				TextCat[i] =  ((IElement) (categories[i])).getTitle();
 				
 				if (((IElement) (categories[i])).getElements() == null)
 				{
 					if (((IElement)(categories[i])).getScore() == "" || ((IElement)(categories[i])).getScore().equalsIgnoreCase("0") )//|| ((IElement)(categories[i])).getScore() =="0") //$NON-NLS-1$ //$NON-NLS-2$
 					{	
 						ScoreCat[i]=0.01;
 					}
 					else
 					{
 						ScoreCat[i]=(Double.valueOf(((IElement)categories[i]).getScore())).doubleValue();
 					}
 				}
 				else
 				{
 					SumScore=0.0;
 					Object ele2[] =    ((IElement) (categories[i])).getElements().toArray();
 					for(j= 0;j<ele2.length;j++)
 					{
 						
 						if (((IElement) (ele2[j])).getElements() == null)
 						{
 							if (((IElement)(ele2[j])).getScore() == "" || ((IElement)(ele2[j])).getScore().equalsIgnoreCase("0") ) //$NON-NLS-1$ //$NON-NLS-2$
 							{	
 								ScoreCat2[j]=Double.valueOf("0.01"); //$NON-NLS-1$
 							}
 							else
 							{
 								ScoreCat2[j]=Double.valueOf( ((IElement)ele2[j]).getScore());
 							}
 						}
 						else
 						{
 							SumScore2=0.0;
 							Object ele3[] =    ((IElement) (ele2[j])).getElements().toArray();
 							for(k= 0;k<ele3.length;k++)
 							{
 								{
 									if (((IElement) (ele3[k])).getElements() == null)
 									{
 										if (((IElement)(ele3[k])).getScore() == "" || ((IElement)(ele3[k])).getScore().equalsIgnoreCase("0") ) //$NON-NLS-1$ //$NON-NLS-2$
 										{	
 											ScoreCat3[k]=Double.valueOf("0.01"); //$NON-NLS-1$
 										}
 										else
 										{
 											ScoreCat3[k]=Double.valueOf( ((IElement)ele3[k]).getScore());
 										}
 									}
 									else
 									{
 										SumScore3=0.0;
 										Object ele4[] =    ((IElement) (ele3[k])).getElements().toArray();
 										for(l= 0;l<ele4.length;l++)
 										{
 											if (((IElement) (ele4[l])).getElements() == null)
 											{
 												if (((IElement)(ele4[l])).getScore() == "" || ((IElement)(ele4[l])).getScore().equalsIgnoreCase("0")) //$NON-NLS-1$ //$NON-NLS-2$
 												{	
 													ScoreCat4[l]=Double.valueOf("0.01"); //$NON-NLS-1$
 												}
 												else
 												{
 													ScoreCat4[l]=Double.valueOf( ((IElement)ele4[l]).getScore());
 												}
 											}
 											else
 											{
 												SumScore4=0.0;
 												Object ele5[] =    ((IElement) (ele4[l])).getElements().toArray();
 												for(m= 0;m<ele5.length;m++)
 												{
 													if (((IElement)(ele5[m])).getScore() == "" || ((IElement)(ele5[m])).getScore().equalsIgnoreCase("0") ) //$NON-NLS-1$ //$NON-NLS-2$
 													{	
 														ScoreCat5[m]=Double.valueOf("0.01"); //$NON-NLS-1$
 													}
 													
 													else
 													{			
 														ScoreCat5[m]=Double.valueOf(((IElement)ele5[m]).getScore());
 													}
 													SumScore4 = SumScore4 + ScoreCat5[m].doubleValue();
 												}
 												double Scores3=(SumScore4 / ele5.length);
 												
 												ScoreCat4[l]=Double.valueOf(String.valueOf(Scores3));
 											}
 											SumScore3= SumScore3+ScoreCat4[l].doubleValue();
 										}
 										double Scores2=(SumScore3 / ele4.length);
 										
 										ScoreCat3[k]=Double.valueOf(String.valueOf(Scores2));
 									}
 									SumScore2= SumScore2+ScoreCat3[k].doubleValue();
 								}
 							}
 							double Scores=(SumScore2 / ele3.length);
 							
 							ScoreCat2[j]=Double.valueOf(String.valueOf(Scores));
 						}
 						SumScore= SumScore+ScoreCat2[j].doubleValue();
 					}
 					ScoreCat[i]=(SumScore/ele2.length);		
 				}
 			}
 			run(TextCat,ScoreCat);
 		} 
 		else
 		{
 			MessageDialog.openWarning(window.getShell(),Messages.getString("GenerateRadar.warning"),Messages.getString("GenerateRadar.warningMessage")); //$NON-NLS-1$ //$NON-NLS-2$
 			
 		}
 	}
 
 	
 	/**
 	 * @return String
 	 */
 	public static String getTitle()
 	{
 		return ((String) ((SheetCTabItem) window.getApp().getCTabFolder().getSelection()).getSaveLibQSOS().getAppname()).concat(((SheetCTabItem) window.getApp().getCTabFolder().getSelection()).getSaveLibQSOS().getRelease());
 	}
 }
 
