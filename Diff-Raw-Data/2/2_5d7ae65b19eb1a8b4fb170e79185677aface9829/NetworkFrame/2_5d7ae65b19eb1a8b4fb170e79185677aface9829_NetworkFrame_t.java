 package org.computer.knauss.reqtDiscussion.ui.visualization.sna;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.sql.Date;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import org.computer.knauss.reqtDiscussion.model.Discussion;
 import org.computer.knauss.reqtDiscussion.model.DiscussionEvent;
 import org.computer.knauss.reqtDiscussion.model.DiscussionFactory;
 import org.computer.knauss.reqtDiscussion.model.metric.AbstractNetworkMetric;
 import org.computer.knauss.reqtDiscussion.model.partition.FixedNumberPartition;
 import org.computer.knauss.reqtDiscussion.model.partition.IDiscussionOverTimePartition;
 import org.computer.knauss.reqtDiscussion.model.partition.TimeIntervalPartition;
 import org.computer.knauss.reqtDiscussion.model.socialNetwork.Node;
 import org.computer.knauss.reqtDiscussion.model.socialNetwork.PartitionedSocialNetwork;
 import org.computer.knauss.reqtDiscussion.model.socialNetwork.ProximitySocialNetwork;
 import org.computer.knauss.reqtDiscussion.model.socialNetwork.SocialNetwork;
 
 public class NetworkFrame extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 	private NetworkPanel networkPanel;
 	private boolean play;
 
 	private TimerTask timer = new TimerTask() {
 
 		@Override
 		public void run() {
 			if (play) {
 				networkPanel.getLayouter().layout(1);
 				networkPanel.repaint();
 			}
 		}
 
 	};
 	private JButton playButton;
 	private JComboBox socialNetworkBox;
 	private JSpinner weightSpinner;
 	private IDiscussionOverTimePartition partition;
 	private Discussion[] discussions;
 	private JSlider zoomSlider;
 	private JLabel metricLabel;
 	private JSlider cutoffSlider;
 
 	public NetworkFrame() {
 		super("Social Network Analysis");
 
 		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
 		setLayout(new BorderLayout());
 
 		JPanel buttonPanel = new JPanel();
 		add(buttonPanel, BorderLayout.NORTH);
 		playButton = new JButton("start");
 		playButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0e) {
 				play = !play;
 
 				if (play)
 					playButton.setText("stop");
 				else
 					playButton.setText("start");
 			}
 		});
 		buttonPanel.add(playButton);
 		this.socialNetworkBox = new JComboBox(new Object[] {
 				new PartitionedSocialNetwork(), new ProximitySocialNetwork() });
 		this.socialNetworkBox.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				setWorkitems(discussions, partition);
 			}
 		});
 		buttonPanel.add(this.socialNetworkBox);
 
 		this.weightSpinner = new JSpinner(new SpinnerNumberModel(0.0d, 0.0d,
 				100.0d, 0.1d));
 		this.weightSpinner.addChangeListener(new ChangeListener() {
 
 			@Override
 			public void stateChanged(ChangeEvent arg0) {
 				setWorkitems(discussions, partition);
				double v = (Double)weightSpinner.getValue();
				cutoffSlider.setValue((int)v);
 			}
 		});
 		buttonPanel.add(this.weightSpinner);
 
 		this.networkPanel = new NetworkPanel();
 		add(new JScrollPane(this.networkPanel), BorderLayout.CENTER);
 
 		JPanel ctrlPanel = new JPanel(new GridLayout(2, 1));
 		add(ctrlPanel, BorderLayout.WEST);
 		this.zoomSlider = new JSlider(2, 20, 10);
 		Dictionary<Integer, JComponent> labels = new Hashtable<Integer, JComponent>();
 		labels.put(2, new JLabel("-"));
 		labels.put(20, new JLabel("+"));
 		this.zoomSlider.setLabelTable(labels);
 		this.zoomSlider.setPaintLabels(true);
 		this.zoomSlider.addChangeListener(new ChangeListener() {
 
 			@Override
 			public void stateChanged(ChangeEvent e) {
 				networkPanel.setZoomFactor(zoomSlider.getValue() / 10d);
 			}
 		});
 		ctrlPanel.add(this.zoomSlider, BorderLayout.WEST);
 
 		this.cutoffSlider = new JSlider();
 		this.cutoffSlider.setPaintLabels(true);
 		this.cutoffSlider.setValue(0);
 		this.cutoffSlider.addChangeListener(new ChangeListener() {
 
 			@Override
 			public void stateChanged(ChangeEvent arg0) {
 				weightSpinner.setValue((double) cutoffSlider.getValue());
 				setWorkitems(discussions, partition);
 			}
 		});
 		ctrlPanel.add(this.cutoffSlider);
 
 		this.metricLabel = new JLabel("");
 		add(this.metricLabel, BorderLayout.SOUTH);
 
 		pack();
 
 		Timer t = new Timer();
 		t.schedule(this.timer, 50, 100);
 	}
 
 	public void setWorkitems(Discussion[] discussions,
 			IDiscussionOverTimePartition partition) {
 		this.discussions = discussions;
 		this.partition = partition;
 
 		if (discussions != null && partition != null) {
 			SocialNetwork sn = (SocialNetwork) this.socialNetworkBox
 					.getSelectedItem();
 			sn.setDiscussionData(discussions, partition);
 			this.networkPanel.setMinWeight((Double) weightSpinner.getValue());
 			this.networkPanel.setNetwork(sn);
 			this.networkPanel.repaint();
 
 			computeNetworkMetrics(discussions, partition, sn);
 			updateCutoffSlider(sn);
 		}
 	}
 
 	private void updateCutoffSlider(SocialNetwork sn) {
 		List<Double> weights = new LinkedList<Double>();
 		double maxWeight = 0;
 		for (Node n1 : sn.getActors()) {
 			for (Node n2 : sn.getActors()) {
 				double weight = sn.getWeight(n1, n2);
 				if (weight > 0) {
 					weights.add(weight);
 					if (weight > maxWeight)
 						maxWeight = weight;
 				}
 			}
 		}
 
 		Collections.sort(weights);
 		int[] bucketAmounts = new int[(int) maxWeight + 1];
 		// 1. divide the maxWeight by bucketAmounts.length, and count how many
 		// weights we
 		// have for each bucket
 		int i = 0;
 		for (Double w : weights) {
 			while (w > i + 1) {
 				i++;
 			}
 			bucketAmounts[i]++;
 		}
 
 		// 2. divide the maxAmount per bucket by the height we might use
 		int maxHeight = 100;
 
 		// 3. create pictures for each bucket and add them to the slider
 		Dictionary<Integer, JComponent> labels = new Hashtable<Integer, JComponent>();
 		for (int j = 0; j < bucketAmounts.length; j++) {
 			int amount = bucketAmounts[j];
 			int maxAmount = weights.size();
 			if (maxHeight < weights.size()) {
 				amount = (amount * maxHeight) / weights.size();
 				maxAmount = maxHeight;
 			} else if (weights.size() == 0 )
 				maxAmount = 1;
 			BufferedImage bi = new BufferedImage(5, maxAmount,
 					BufferedImage.TYPE_INT_RGB);
 			// System.out.println(j + " = " + amount + "/" + weights.size());
 			Graphics graphics = bi.getGraphics();
 			graphics.fillRect(0, 0, 5, maxAmount);
 			graphics.setColor(Color.BLUE);
 
 			graphics.fillRect(0, 0, 5, amount);
 			JLabel weightLabel = new JLabel(new ImageIcon(bi));
 			weightLabel.setToolTipText(amount + " edges have weight < " + j);
 			labels.put(j, weightLabel);
 		}
 		this.cutoffSlider.setMinimum(0);
 		this.cutoffSlider.setMaximum((int) maxWeight);
 		this.cutoffSlider.setLabelTable(labels);
 	}
 
 	private void computeNetworkMetrics(Discussion[] discussions,
 			IDiscussionOverTimePartition partition, SocialNetwork sn) {
 		StringBuffer sb = new StringBuffer();
 		for (AbstractNetworkMetric anm : AbstractNetworkMetric.STANDARD_METRICS) {
 			sb.append(" | ");
 			sb.append(anm.getName());
 			sb.append(" = ");
 			anm.setPartition(partition);
 			anm.setSocialNetwork(sn);
 			anm.setMinWeight((Double) weightSpinner.getValue());
 			sb.append(String.valueOf(anm.getDecimalFormat().format(
 					anm.considerDiscussions(discussions))));
 		}
 		sb.append(" | ");
 		this.metricLabel.setText(sb.toString());
 	}
 
 	public static void main(String[] args) {
 		NetworkFrame f = new NetworkFrame();
 
 		Random r = new Random();
 		int actorNumber = 7;
 		DiscussionEvent[] wcs = new DiscussionEvent[20];
 		for (int i = 0; i < wcs.length; i++) {
 			wcs[i] = new DiscussionEvent();
 			wcs[i].setCreator("user" + r.nextInt(actorNumber));
 			wcs[i].setCreationDate(new Date(System.currentTimeMillis()
 					- r.nextInt(40) * TimeIntervalPartition.MILLIS_PER_DAY));
 		}
 
 		Arrays.sort(wcs, new Comparator<DiscussionEvent>() {
 
 			@Override
 			public int compare(DiscussionEvent o1, DiscussionEvent o2) {
 				return o1.getCreationDate().compareTo(o2.getCreationDate());
 			}
 		});
 
 		Discussion d = DiscussionFactory.getInstance().getDiscussion(1);
 
 		d.addComments(wcs);
 		d.setDateCreated(wcs[0].getCreationDate());
 		FixedNumberPartition p = new FixedNumberPartition();
 		p.setTimeInterval(wcs[0].getCreationDate(),
 				wcs[wcs.length - 1].getCreationDate());
 
 		f.setWorkitems(new Discussion[] { d }, p);
 		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		f.pack();
 		f.setVisible(true);
 	}
 }
