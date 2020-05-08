 package cadenza.gui.wizard;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
import javax.swing.Icon;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 
 import org.ciscavate.cjwizard.WizardPage;
 import org.ciscavate.cjwizard.pagetemplates.PageTemplate;
 import org.ciscavate.cjwizard.pagetemplates.TitledPageTemplate;
 
 import common.swing.SwingUtils;
import common.swing.icon.ArrowIcon;
 
 public class OverviewPageTemplate extends PageTemplate {
	public static Icon ICON = new ArrowIcon(SwingConstants.EAST);
	
 	private final JList<WizardPage> _overview;
 	private final PageTemplate _innerTemplate;
 	private final CadenzaWizardPageFactory _pageFactory;
 	
 	public OverviewPageTemplate(CadenzaWizardPageFactory pageFactory) {
 		_pageFactory = pageFactory;
 		_innerTemplate = new TitledPageTemplate();
 		_overview = new JList<>(pageFactory.getPages());
 		
 		_overview.setOpaque(false);
 		_overview.setEnabled(false);
 		_overview.setCellRenderer(new OverviewRenderer());
 		_overview.setBorder(BorderFactory.createCompoundBorder(
 				BorderFactory.createEmptyBorder(4, 4, 1, 4), BorderFactory.createLineBorder(Color.BLACK)));
 		SwingUtils.freezeWidth(_overview, 250);
 		final JPanel west = new JPanel();
 		west.setLayout(new BoxLayout(west, BoxLayout.Y_AXIS));
 		west.add(_overview);
 		
 		setLayout(new BorderLayout());
 		add(_overview, BorderLayout.WEST);
 		add(_innerTemplate, BorderLayout.CENTER);
 	}
 
 	@Override
 	public void setPage(final WizardPage page) {
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				_innerTemplate.setPage(page);
 				_overview.setSelectedIndex(_pageFactory.getPageIndex(page));
 			}
 		});
 	}
 	
 	private class OverviewRenderer extends JLabel implements ListCellRenderer<WizardPage> {
 		public OverviewRenderer() {
 			super();
 			setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
 		}
 		
 		@Override
 		public Component getListCellRendererComponent(
 				JList<? extends WizardPage> list, WizardPage value, int index,
 				boolean isSelected, boolean cellHasFocus) {
 			setText(value.getTitle());
			setIcon(isSelected ? ICON : null);
 			
 			return this;
 		}
 	}
 }
