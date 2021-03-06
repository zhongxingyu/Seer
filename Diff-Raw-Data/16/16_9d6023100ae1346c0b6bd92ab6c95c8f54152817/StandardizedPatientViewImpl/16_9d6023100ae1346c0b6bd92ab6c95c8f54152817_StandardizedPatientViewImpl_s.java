 /**
  * 
  */
 package ch.unibas.medizin.osce.client.a_nonroo.client.ui.sp;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import ch.unibas.medizin.osce.client.a_nonroo.client.OsMaConstant;
 import ch.unibas.medizin.osce.client.a_nonroo.client.i18n.Messages;
 import ch.unibas.medizin.osce.client.managed.request.StandardizedPatientProxy;
 import ch.unibas.medizin.osce.client.style.interfaces.MyCellTableResources;
 import ch.unibas.medizin.osce.client.style.interfaces.MySimplePagerResources;
 import ch.unibas.medizin.osce.client.style.widgets.IconButton;
 
 import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.StyleInjector;
 import com.google.gwt.event.dom.client.BlurEvent;
 import com.google.gwt.event.dom.client.BlurHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.FocusEvent;
 import com.google.gwt.event.dom.client.FocusHandler;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.text.client.DateTimeFormatRenderer;
 import com.google.gwt.text.shared.AbstractRenderer;
 import com.google.gwt.text.shared.Renderer;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.SimplePager;
 import com.google.gwt.user.cellview.client.TextColumn;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.SplitLayoutPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * @author nikotsunami
  *
  */
 public class StandardizedPatientViewImpl extends Composite implements  StandardizedPatientView {
 
 	private static SystemStartViewUiBinder uiBinder = GWT
 			.create(SystemStartViewUiBinder.class);
 
 	interface SystemStartViewUiBinder extends UiBinder<Widget, StandardizedPatientViewImpl> {
 	}
 
 	private Delegate delegate;
 
 	@UiField
 	SplitLayoutPanel splitLayoutPanel;
 	
 	@UiField
 	TextBox searchBox;
 	
 	@UiField
 	IconButton newButton;
 	
 	@UiField
 	SimplePanel detailsPanel;
 
 	@UiField(provided = true)
 	SimplePager pager;
 	
 	@UiField(provided = true)
 	CellTable<StandardizedPatientProxy> table;
 
 	protected Set<String> paths = new HashSet<String>();
 
 	private Presenter presenter;
 
 	@UiHandler ("newButton")
 	public void newButtonClicked(ClickEvent event) {
 		delegate.newClicked();
 	}
 
 	/**
 	 * Because this class has a default constructor, it can
 	 * be used as a binder template. In other words, it can be used in other
 	 * *.ui.xml files as follows:
 	 * <ui:UiBinder xmlns:ui="urn:ui:com.google.gwt.uibinder"
 	 *   xmlns:g="urn:import:**user's package**">
 	 *  <g:**UserClassName**>Hello!</g:**UserClassName>
 	 * </ui:UiBinder>
 	 * Note that depending on the widget that is used, it may be necessary to
 	 * implement HasHTML instead of HasText.
 	 */
 	public StandardizedPatientViewImpl() {
 		CellTable.Resources tableResources = GWT.create(MyCellTableResources.class);
 		table = new CellTable<StandardizedPatientProxy>(OsMaConstant.TABLE_PAGE_SIZE, tableResources);
 		
 		SimplePager.Resources pagerResources = GWT.create(MySimplePagerResources.class);
 		pager = new SimplePager(SimplePager.TextLocation.RIGHT, pagerResources, true, OsMaConstant.TABLE_JUMP_SIZE, true);
 		
 		initWidget(uiBinder.createAndBindUi(this));
 		init();
 		splitLayoutPanel.setWidgetMinSize(splitLayoutPanel.getWidget(0), OsMaConstant.SPLIT_PANEL_MINWIDTH);
 		newButton.setText(Messages.ADD_PATIENT);
 	}
 
 	public String[] getPaths() {
 		return paths.toArray(new String[paths.size()]);
 	}
 
 	public void init() {
		StyleInjector.inject(".cellTableOddRow {background: #00ff00;}");
 		searchBox.addFocusHandler(new FocusHandler() {
 			@Override
 			public void onFocus(FocusEvent arg0) {
 				searchBox.setValue("");
 			}
 		});
 		searchBox.addBlurHandler(new BlurHandler() {
 			@Override
 			public void onBlur(BlurEvent arg0) {
 				if(searchBox.getValue().isEmpty()) {
 					searchBox.setValue("Suche...");
 				}
 			}
 		});
 		searchBox.addKeyUpHandler(new KeyUpHandler() {
 			@Override
 			public void onKeyUp(KeyUpEvent arg0) {
 				String q = searchBox.getValue();
 				delegate.performSearch(q);
 			}
 		});
 		
 		// bugfix to avoid hiding of all panels (maybe there is a better solution...?!)
 		DOM.setElementAttribute(splitLayoutPanel.getElement(), "style", "position: absolute; left: 0px; top: 0px; right: 5px; bottom: 0px;");
 //    	paths.add("id");
 //        table.addColumn(new TextColumn<StandardizedPatientProxy>() {
 //
 //            Renderer<java.lang.Long> renderer = new AbstractRenderer<java.lang.Long>() {
 //
 //                public String render(java.lang.Long obj) {
 //                    return obj == null ? "" : String.valueOf(obj);
 //                }
 //            };
 //
 //            @Override
 //            public String getValue(StandardizedPatientProxy object) {
 //                return renderer.render(object.getId());
 //            }
 //        }, "Id");
 //        paths.add("version");
 //        table.addColumn(new TextColumn<StandardizedPatientProxy>() {
 //
 //            Renderer<java.lang.Integer> renderer = new AbstractRenderer<java.lang.Integer>() {
 //
 //                public String render(java.lang.Integer obj) {
 //                    return obj == null ? "" : String.valueOf(obj);
 //                }
 //            };
 //
 //            @Override
 //            public String getValue(StandardizedPatientProxy object) {
 //                return renderer.render(object.getVersion());
 //            }
 //        }, "Version");
 //        paths.add("gender");
 //        table.addColumn(new TextColumn<StandardizedPatientProxy>() {
 //
 //            Renderer<java.lang.String> renderer = new AbstractRenderer<java.lang.String>() {
 //
 //                public String render(java.lang.String obj) {
 //                    return obj == null ? "" : String.valueOf(obj);
 //                }
 //            };
 //
 //            @Override
 //            public String getValue(StandardizedPatientProxy object) {
 //                return renderer.render(object.getGender().toString());
 //            }
 //        }, "Gender");
 		paths.add("name");
 		table.addColumn(new TextColumn<StandardizedPatientProxy>() {
 
 			Renderer<java.lang.String> renderer = new AbstractRenderer<java.lang.String>() {
 
 				public String render(java.lang.String obj) {
 					return obj == null ? "" : String.valueOf(obj);
 				}
 			};
 
 			@Override
 			public String getValue(StandardizedPatientProxy object) {
 				return renderer.render(object.getName());
 			}
 		}, "Name");
 		paths.add("preName");
 		table.addColumn(new TextColumn<StandardizedPatientProxy>() {
 
 			Renderer<java.lang.String> renderer = new AbstractRenderer<java.lang.String>() {
 
 				public String render(java.lang.String obj) {
 					return obj == null ? "" : String.valueOf(obj);
 				}
 			};
 
 			@Override
 			public String getValue(StandardizedPatientProxy object) {
 				return renderer.render(object.getPreName());
 			}
 		}, "Pre Name");
 //        paths.add("street");
 //        table.addColumn(new TextColumn<StandardizedPatientProxy>() {
 //
 //            Renderer<java.lang.String> renderer = new AbstractRenderer<java.lang.String>() {
 //
 //                public String render(java.lang.String obj) {
 //                    return obj == null ? "" : String.valueOf(obj);
 //                }
 //            };
 //
 //            @Override
 //            public String getValue(StandardizedPatientProxy object) {
 //                return renderer.render(object.getStreet());
 //            }
 //        }, "Street");
 //        paths.add("city");
 //        table.addColumn(new TextColumn<StandardizedPatientProxy>() {
 //
 //            Renderer<java.lang.String> renderer = new AbstractRenderer<java.lang.String>() {
 //
 //                public String render(java.lang.String obj) {
 //                    return obj == null ? "" : String.valueOf(obj);
 //                }
 //            };
 //
 //            @Override
 //            public String getValue(StandardizedPatientProxy object) {
 //                return renderer.render(object.getCity());
 //            }
 //        }, "City");
 //        paths.add("postalCode");
 //        table.addColumn(new TextColumn<StandardizedPatientProxy>() {
 //
 //            Renderer<java.lang.Integer> renderer = new AbstractRenderer<java.lang.Integer>() {
 //
 //                public String render(java.lang.Integer obj) {
 //                    return obj == null ? "" : String.valueOf(obj);
 //                }
 //            };
 //
 //            @Override
 //            public String getValue(StandardizedPatientProxy object) {
 //                return renderer.render(object.getPostalCode());
 //            }
 //        }, "Postal Code");
 //        paths.add("telephone");
 //        table.addColumn(new TextColumn<StandardizedPatientProxy>() {
 //
 //            Renderer<java.lang.String> renderer = new AbstractRenderer<java.lang.String>() {
 //
 //                public String render(java.lang.String obj) {
 //                    return obj == null ? "" : String.valueOf(obj);
 //                }
 //            };
 //
 //            @Override
 //            public String getValue(StandardizedPatientProxy object) {
 //                return renderer.render(object.getTelephone());
 //            }
 //        }, "Telephone");
 //        paths.add("mobile");
 //        table.addColumn(new TextColumn<StandardizedPatientProxy>() {
 //
 //            Renderer<java.lang.String> renderer = new AbstractRenderer<java.lang.String>() {
 //
 //                public String render(java.lang.String obj) {
 //                    return obj == null ? "" : String.valueOf(obj);
 //                }
 //            };
 //
 //            @Override
 //            public String getValue(StandardizedPatientProxy object) {
 //                return renderer.render(object.getMobile());
 //            }
 //        }, "Mobile");
 //        paths.add("birthday");
 //        table.addColumn(new TextColumn<StandardizedPatientProxy>() {
 //
 //            Renderer<java.util.Date> renderer = new DateTimeFormatRenderer(DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT));
 //
 //            @Override
 //            public String getValue(StandardizedPatientProxy object) {
 //                return renderer.render(object.getBirthday());
 //            }
 //        }, "Birthday");
 		paths.add("email");
 		table.addColumn(new TextColumn<StandardizedPatientProxy>() {
 
 			Renderer<java.lang.String> renderer = new AbstractRenderer<java.lang.String>() {
 
 				public String render(java.lang.String obj) {
 					return obj == null ? "" : String.valueOf(obj);
 				}
 			};
 
 			@Override
 			public String getValue(StandardizedPatientProxy object) {
 				return renderer.render(object.getEmail());
 			}
 		}, "Email");
 //        paths.add("nationality");
 //        table.addColumn(new TextColumn<StandardizedPatientProxy>() {
 //
 //            Renderer<ch.unibas.medizin.osce.client.managed.request.NationalityProxy> renderer = ch.unibas.medizin.osce.client.managed.ui.NationalityProxyRenderer.instance();
 //
 //            @Override
 //            public String getValue(StandardizedPatientProxy object) {
 //                return renderer.render(object.getNationality());
 //            }
 //        }, "Nationality");
 //        paths.add("profession");
 //        table.addColumn(new TextColumn<StandardizedPatientProxy>() {
 //
 //            Renderer<ch.unibas.medizin.osce.client.managed.request.ProfessionProxy> renderer = ch.unibas.medizin.osce.client.managed.ui.ProfessionProxyRenderer.instance();
 //
 //            @Override
 //            public String getValue(StandardizedPatientProxy object) {
 //                return renderer.render(object.getProfession());
 //            }
 //        }, "Profession");
 //        paths.add("langskills");
 //        table.addColumn(new TextColumn<StandardizedPatientProxy>() {
 //
 //            Renderer<java.util.Set> renderer = ch.unibas.medizin.osce.client.scaffold.place.CollectionRenderer.of(ch.unibas.medizin.osce.client.managed.ui.LangSkillProxyRenderer.instance());
 //
 //            @Override
 //            public String getValue(StandardizedPatientProxy object) {
 //                return renderer.render(object.getLangskills());
 //            }
 //        }, "Langskills");
 //        paths.add("bankAccount");
 //        table.addColumn(new TextColumn<StandardizedPatientProxy>() {
 //
 //            Renderer<ch.unibas.medizin.osce.client.managed.request.BankaccountProxy> renderer = ch.unibas.medizin.osce.client.managed.ui.BankaccountProxyRenderer.instance();
 //
 //            @Override
 //            public String getValue(StandardizedPatientProxy object) {
 //                return renderer.render(object.getBankAccount());
 //            }
 //        }, "Bank Account");
 //        paths.add("descriptions");
 //        table.addColumn(new TextColumn<StandardizedPatientProxy>() {
 //
 //            Renderer<ch.unibas.medizin.osce.client.managed.request.DescriptionProxy> renderer = ch.unibas.medizin.osce.client.managed.ui.DescriptionProxyRenderer.instance();
 //
 //            @Override
 //            public String getValue(StandardizedPatientProxy object) {
 //                return renderer.render(object.getDescriptions());
 //            }
 //        }, "Descriptions");
 //        paths.add("anamnesisForm");
 //        table.addColumn(new TextColumn<StandardizedPatientProxy>() {
 //
 //            Renderer<ch.unibas.medizin.osce.client.managed.request.AnamnesisFormProxy> renderer = ch.unibas.medizin.osce.client.managed.ui.AnamnesisFormProxyRenderer.instance();
 //
 //            @Override
 //            public String getValue(StandardizedPatientProxy object) {
 //                return renderer.render(object.getAnamnesisForm());
 //            }
 //        }, "Anamnesis Form");
 	}
 
 	@Override
 	public CellTable<StandardizedPatientProxy> getTable() {
 		return table;
 	}
 
 	@Override
 	public void setDelegate(Delegate delegate) {
 		this.delegate = delegate;
 	}
 
 	@Override
 	public SimplePanel getDetailsPanel() {
 		return detailsPanel;
 	}
 
 	@Override
 	public void setPresenter(Presenter presenter) {
 		this.presenter = presenter;
 	}
 }
