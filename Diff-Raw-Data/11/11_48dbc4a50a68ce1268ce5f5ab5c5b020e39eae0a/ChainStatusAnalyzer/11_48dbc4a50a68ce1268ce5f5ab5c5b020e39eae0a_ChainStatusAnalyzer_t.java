 package de.ptb.epics.eve.viewer;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import de.ptb.epics.eve.data.scandescription.Chain;
 import de.ptb.epics.eve.data.scandescription.ScanModule;
 import de.ptb.epics.eve.ecp1.client.interfaces.IChainStatusListener;
 import de.ptb.epics.eve.ecp1.client.interfaces.IEngineStatusListener;
 import de.ptb.epics.eve.ecp1.intern.ChainStatusCommand;
 import de.ptb.epics.eve.ecp1.intern.EngineStatus;
 
 public class ChainStatusAnalyzer implements IEngineStatusListener, IChainStatusListener {
 
 	private final List< Chain > idleChains;
 	private final List< Chain > runningChains;
 	private final List< Chain > exitedChains;
 	private final List< ScanModule > initializingScanModules;
 	private final List< ScanModule > executingScanModules;
 	private final List< ScanModule > pausedScanModules;
 	private final List< ScanModule > waitingScanModules;
 	private final List< ScanModule > exitedScanModules;
 
 	private final List< IUpdateListener > updateListener;
 	
 	public ChainStatusAnalyzer() {
 
 		this.idleChains = new ArrayList< Chain >();
 		this.runningChains = new ArrayList< Chain >();
 		this.exitedChains = new ArrayList< Chain >();
 		
 		this.initializingScanModules = new ArrayList< ScanModule >();
 		this.executingScanModules = new ArrayList< ScanModule >();
 		this.pausedScanModules = new ArrayList< ScanModule >();
 		this.waitingScanModules = new ArrayList< ScanModule >();
 		this.exitedScanModules = new ArrayList< ScanModule >();
 		
 		this.updateListener = new ArrayList< IUpdateListener >();
 
 	}
 	
 	@Override
 	public void engineStatusChanged(EngineStatus engineStatus, String xmlName, int repeatCount) {
 
		if (engineStatus == EngineStatus.LOADING_XML || 
			engineStatus == EngineStatus.IDLE_XML_LOADED) {
 			// Es wird gerade ein neues XML-File geladen, ChainStatusListe löschen
 			this.resetChainList();
 
 			final Iterator< IUpdateListener > it = this.updateListener.iterator();
 			while( it.hasNext() ) {
 				it.next().setLoadedScmlFile(xmlName);
 			}
 		}
 		else {
 			// bei allen anderen Engine Status Meldungen wird gesetzt, was gemacht werden darf.
 			final Iterator< IUpdateListener > it = this.updateListener.iterator();
 			while( it.hasNext() ) {
 				it.next().fillEngineStatus(engineStatus, repeatCount);
 			}
 		}
 	}
 
 	public void setAutoPlayStatus(boolean autoPlayStatus) {
 
 		final Iterator< IUpdateListener > it = this.updateListener.iterator();
 		while( it.hasNext() ) {
 			it.next().setAutoPlayStatus(autoPlayStatus);
 		}
 	
 	}
 	
 	public void chainStatusChanged( final ChainStatusCommand chainStatusCommand ) {
 
 		if( Activator.getDefault().getCurrentScanDescription() == null ) {
 			// TODO: Nach Umstellung der Meldungen darf die ScanDescription nicht mehr null sein 13.12.10 Hartmut
 			// dann kann auch fillStatusTable aus ...epics/eve/viewer/IUpdateListener.java ausgetragen werden
 			
 			switch( chainStatusCommand.getChainStatus() ) {
 				case STARTING_SM:
 					final Iterator< IUpdateListener > it = this.updateListener.iterator();
 					while( it.hasNext() ) {
 						it.next().fillStatusTable(chainStatusCommand.getChainId(), chainStatusCommand.getScanModulId(), "initialized", chainStatusCommand.getRemainingTime());
 					}
 					break;
 			}
 
 			return;
 		}
 		List< Chain > chains = null;
 		
 		switch( chainStatusCommand.getChainStatus() ) {
 			case IDLE:
 				chains = Activator.getDefault().getCurrentScanDescription().getChains();
 				for( int i = 0; i < chains.size(); ++i ) {
 					if( chains.get( i ).getId() == chainStatusCommand.getChainId() ) {
 						this.idleChains.add( chains.get( i ) );
 						this.runningChains.remove( chains.get( i ) );
 						this.exitedChains.remove( chains.get( i ) );
 						break;
 					}
 				}
 				break;
 				
 			case STARTING_SM:
 				chains = Activator.getDefault().getCurrentScanDescription().getChains();
 				for( int i = 0; i < chains.size(); ++i ) {
 					if( chains.get( i ).getId() == chainStatusCommand.getChainId() ) {
 						List< ScanModule > scanModules = chains.get( i ).getScanModules();
 						for( int j = 0; j < scanModules.size(); ++j ) {
 							if( scanModules.get( j ).getId() == chainStatusCommand.getScanModulId() ) {
 								this.initializingScanModules.add( scanModules.get( j ) );
 								this.executingScanModules.remove( scanModules.get( j ) );
 								this.pausedScanModules.remove( scanModules.get( j ) );
 								this.waitingScanModules.remove( scanModules.get( j ) );
 								this.exitedScanModules.remove( scanModules.get( j ) );
 							}
 						}
 					}
 				}
 				break;
 				
 			case EXECUTING_SM:
 				chains = Activator.getDefault().getCurrentScanDescription().getChains();
 				for( int i = 0; i < chains.size(); ++i ) {
 					if( chains.get( i ).getId() == chainStatusCommand.getChainId() ) {
 						List< ScanModule > scanModules = chains.get( i ).getScanModules();
 						for( int j = 0; j < scanModules.size(); ++j ) {
 							if( scanModules.get( j ).getId() == chainStatusCommand.getScanModulId() ) {
 								this.initializingScanModules.remove( scanModules.get( j ) );
 								this.executingScanModules.add( scanModules.get( j ) );
 								this.pausedScanModules.remove( scanModules.get( j ) );
 								this.waitingScanModules.remove( scanModules.get( j ) );
 								this.exitedScanModules.remove( scanModules.get( j ) );
 								
 								this.idleChains.remove( scanModules.get( j ).getChain() );
 								if (!this.runningChains.contains(scanModules.get( j ).getChain())) {
 									// chain i noch nicht in der Liste vorhanden
 									this.runningChains.add( scanModules.get( j ).getChain() );
 								};
 //								this.runningChains.add( scanModules.get( j ).getChain() );
 //								System.out.println("      chain " + i + " zur running Liste hinzugefügt");
 							}
 //							final PlotWindow[] plotWindows = scanModules.get( j ).getPlotWindows();
 //							for( int k = 0; k < plotWindows.length; ++k ) {
 //								System.out.println( "Verarbeite Plot Fenster: " + plotWindows[i].getId() );
 //								final PlotWindow plotWindow = plotWindows[k];
 //								Activator.getDefault().getWorkbench().getDisplay().syncExec( new Runnable() {
 //
 //									public void run() {
 //										
 //										IViewReference[] ref = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().getActivePart().getSite().getPage().getViewReferences();
 //										PlotView view = null;
 //										for( int l = 0; l < ref.length; ++l ) {
 //											if( ref[l].getId().equals( PlotView.ID ) ) {
 //												view = (PlotView)ref[l].getPart( false );
 //												if( view.getId() == -1 ) {
 //													view.setId( plotWindow.getId() );
 //													view.setPlotWindow( plotWindow );
 //													break;
 //												} else if( view.getId() == plotWindow.getId() ) {
 //													view.setPlotWindow( plotWindow );
 //													break;
 //												}
 //												view = null;
 //											}
 //											if( view == null ) {
 //												// Neuen View erzeugen
 //											}
 //										}
 //										
 //									}
 //									
 //								});
 //								
 //							}
 						}
 					}
 				}
 				break;
 				
 			case SM_PAUSED:
 				chains = Activator.getDefault().getCurrentScanDescription().getChains();
 				for( int i = 0; i < chains.size(); ++i ) {
 					if( chains.get( i ).getId() == chainStatusCommand.getChainId() ) {
 						List< ScanModule > scanModules = chains.get( i ).getScanModules();
 						for( int j = 0; j < scanModules.size(); ++j ) {
 							if( scanModules.get( j ).getId() == chainStatusCommand.getScanModulId() ) {
 								this.initializingScanModules.remove( scanModules.get( j ) );
 								this.executingScanModules.remove( scanModules.get( j ) );
 								this.pausedScanModules.add( scanModules.get( j ) );
 								this.waitingScanModules.remove( scanModules.get( j ) );
 								this.exitedScanModules.remove( scanModules.get( j ) );
 								
 								this.idleChains.add( scanModules.get( j ).getChain() );
 								this.runningChains.remove( scanModules.get( j ).getChain() );
 							}
 						}
 					}
 				}
 				break;
 				
 			case WAITING_FOR_MANUAL_TRIGGER:
 				chains = Activator.getDefault().getCurrentScanDescription().getChains();
 				for( int i = 0; i < chains.size(); ++i ) {
 					if( chains.get( i ).getId() == chainStatusCommand.getChainId() ) {
 						List< ScanModule > scanModules = chains.get( i ).getScanModules();
 						for( int j = 0; j < scanModules.size(); ++j ) {
 							if( scanModules.get( j ).getId() == chainStatusCommand.getScanModulId() ) {
 								this.initializingScanModules.remove( scanModules.get( j ) );
 								this.executingScanModules.remove( scanModules.get( j ) );
 								this.pausedScanModules.remove( scanModules.get( j ) );
 								this.waitingScanModules.add( scanModules.get( j ) );
 								this.exitedScanModules.add( scanModules.get( j ) );
 								
 								this.idleChains.add( scanModules.get( j ).getChain() );
 								this.runningChains.remove( scanModules.get( j ).getChain() );
 							}
 						}
 					}
 				}
 				break;
 				
 			case EXITING_SM:
 				chains = Activator.getDefault().getCurrentScanDescription().getChains();
 				for( int i = 0; i < chains.size(); ++i ) {
 					if( chains.get( i ).getId() == chainStatusCommand.getChainId() ) {
 						List< ScanModule > scanModules = chains.get( i ).getScanModules();
 						for( int j = 0; j < scanModules.size(); ++j ) {
 							if( scanModules.get( j ).getId() == chainStatusCommand.getScanModulId() ) {
 								this.initializingScanModules.remove( scanModules.get( j ) );
 								this.executingScanModules.remove( scanModules.get( j ) );
 								this.pausedScanModules.remove( scanModules.get( j ) );
 								this.waitingScanModules.remove( scanModules.get( j ) );
 								this.exitedScanModules.add( scanModules.get( j ) );
 								
 								// Dadurch das ein SM beendet ist, ist nicht auch die chain beendet
 								// this.idleChains.add( scanModules.get( j ).getChain() );
 								// this.runningChains.remove( scanModules.get( j ).getChain() );
 							}
 						}
 					}
 				}
 				break;
 				
 			case EXITING_CHAIN:
 				chains = Activator.getDefault().getCurrentScanDescription().getChains();
 				for( int i = 0; i < chains.size(); ++i ) {
 					if( chains.get( i ).getId() == chainStatusCommand.getChainId() ) {
 						this.idleChains.remove( chains.get( i ) );
 						this.runningChains.remove( chains.get( i ) );
 						this.exitedChains.add( chains.get( i ) );
 					}
 				}
 				break;
 
 			case STORAGE_DONE:
 				final Iterator< IUpdateListener > it2 = this.updateListener.iterator();
 				while( it2.hasNext() ) {
 					it2.next().disableSendToFile();
 				}
 				break;
 				
 		}
 		final Iterator< IUpdateListener > it = this.updateListener.iterator();
 		while( it.hasNext() ) {
 			it.next().updateOccured(chainStatusCommand.getRemainingTime());
 		}
 
 	}
 
 	public boolean addUpdateListener( final IUpdateListener updateListener ) {
 		return this.updateListener.add( updateListener );
 	}
 
 	public boolean remove( final IUpdateListener updateListener ) {
 		return this.updateListener.remove( updateListener );
 	}
 	
 	
 	public List< Chain > getIdleChains() {
 		return new ArrayList< Chain >( this.idleChains );
 	}
 	
 	public List< Chain > getRunningChains() {
 		return new ArrayList< Chain >( this.runningChains );
 	}
 
 	public List< Chain > getExitedChains() {
 		return new ArrayList< Chain >( this.exitedChains );
 	}
 	
 	public List< ScanModule > getInitializingScanModules() {
 		return new ArrayList< ScanModule >( this.initializingScanModules );
 	}
 	
 	public List< ScanModule > getExecutingScanModules() {
 		return new ArrayList< ScanModule >( this.executingScanModules );
 	}
 	
 	public List< ScanModule > getPausedScanModules() {
 		return new ArrayList< ScanModule >( this.pausedScanModules );
 	}
 	
 	public List< ScanModule > getWaitingScanModules() {
 		return new ArrayList< ScanModule >( this.waitingScanModules );
 	}
 
 	public List< ScanModule > getExitingScanModules() {
 		return new ArrayList< ScanModule >( this.exitedScanModules );
 	}
 
 	private void resetChainList() {
 		this.idleChains.clear();
 		this.runningChains.clear();
 		this.exitedChains.clear();
 
 		this.executingScanModules.clear();
 		this.initializingScanModules.clear();
 		this.pausedScanModules.clear();
 		this.waitingScanModules.clear();
 		this.exitedScanModules.clear();
 
 		final Iterator< IUpdateListener > it = this.updateListener.iterator();
 		while( it.hasNext() ) {
 			it.next().clearStatusTable();
 		}
 	}
 }
