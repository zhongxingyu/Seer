 package net.javacrumbs.fjdemo;
 //
 //Copyright  2008-10  The original author or authors
 //
 //Licensed under the Apache License, Version 2.0 (the "License");
 //you may not use this file except in compliance with the License.
 //You may obtain a copy of the License at
 //
 //    http://www.apache.org/licenses/LICENSE-2.0
 //
 //Unless required by applicable law or agreed to in writing, software
 //distributed under the License is distributed on an "AS IS" BASIS,
 //WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 //See the License for the specific language governing permissions and
 //limitations under the License.
 
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.SwingUtilities;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import jsr166y.ForkJoinPool;
 import jsr166y.RecursiveAction;
 
 /**
  * Shows use of the ForkJoin mechanics to implement merge sort.
  *
  * Author: Vaclav Pech, Lukas Krecan, Pavel Jetensky
  * Date: Mar 26, 2011
  */
 
 public class VisualForkJoinMergeSort
 {
 	private static final int ROW_HEIGHT = 40;
 	private static final int COL_WIDTH = 35;
 	private static final Color COLOR_WAIT = new Color(212,146,52);
 	private static final Color COLOR_SCHEDULED = new Color(134,219,52);
 	private static final Color COLOR_FINISHED = Color.GRAY;
 	
 	private final Random random = new Random();
 
 	private static final Color[] THREAD_COLORS = new Color[]{Color.YELLOW, new Color(76,160,255), Color.CYAN, Color.MAGENTA, Color.RED, Color.PINK, Color.ORANGE, Color.WHITE};
 
 	private JPanel panel; 
 	
 	private JSlider numThreads;
 	
 	private JSlider problemSize;
 	
 	private JSlider speed;
 	
 	private JButton startButton; 
 	
 	private JCheckBox randomCheckBox = new JCheckBox("Random data",false);;
 	
 	private JCheckBox randomDelayCheckBox = new JCheckBox("Random speed",false);;
 	
 	
 	private JSlider createSlider(int min, int max, int value, final String message)
 	{
 		final JSlider result = new JSlider(min, max, value);
 		result.setPaintTicks(true);
 		result.setPaintLabels(true);
 		result.setMinorTickSpacing(1);
 		final TitledBorder border = BorderFactory.createTitledBorder(message+" "+result.getValue());
 		result.setBorder(border);
 		result.addChangeListener(new ChangeListener(){
 			public void stateChanged(ChangeEvent event) {
 				border.setTitle(message+" "+result.getValue());
 			}
 		});
 		return result;
 	}
 	
 	
 	private JLabel newLabel(String text, Color color)
 	{
 		JLabel result = new JLabel(text);
 		result.setBackground(color);
 		result.setOpaque(true);
 		result.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
 		return result;
 	}
 	public void start()
 	{
 		JFrame frame = new JFrame("Visualisation of merge sort using fork join");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setSize(1024,640);
 		
 		frame.setLayout(new BorderLayout());
 		
 		panel = new JPanel();
 		panel.setLayout(null);
 		
 		
 		numThreads = createSlider(1, 8, 3, "Number of threads");
 		problemSize = createSlider(4, 64, 32, "Problem size");
 		speed = new JSlider(0, 1000, 700);
 		speed.setBorder(BorderFactory.createTitledBorder("Speed"));
 		
 		//Creates Start Button
 		startButton = new JButton("Start");
 		startButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				numThreads.setEnabled(false);
 				problemSize.setEnabled(false);
 				startButton.setEnabled(false);
 				panel.setPreferredSize(new Dimension(problemSize.getValue()*COL_WIDTH, 7*ROW_HEIGHT));
 				panel.removeAll();
 				new Thread(new Runnable() {
 					public void run() {
 						runDemo();
 					}
 				}).start();
 			}
 		});
 		
 		
 		Box vbox = Box.createVerticalBox();
 		
 		Box hbox1 = Box.createHorizontalBox();
 		hbox1.add(new JLabel("<html><ol><li>Thread takes a task from the queue. If the tasks is too big (longer than two elements in our case) it is split to two smaller tasks</li><li>The subtasks are placed to the queue to be processed</li> <li>While the task waits for its subtasks to finish, the thread is free to take another task from the queue (step 1.)</li> <li>When the subtasks are finished their results are merged</li> </ol> </html>"));
 		vbox.add(hbox1);
 		
 		JPanel panel1 = new JPanel();
 		panel1.add(newLabel("Waiting in queue", COLOR_SCHEDULED));
 		panel1.add(newLabel("Waiting for subtasks", COLOR_WAIT));
 		panel1.add(newLabel("Finished", COLOR_FINISHED));
 		for (int i = 0; i < THREAD_COLORS.length; i++) {
 			panel1.add(newLabel("Thread "+(i+1), THREAD_COLORS[i]));
 		}
 		vbox.add(panel1);
 		
 		Box hbox2 = Box.createHorizontalBox();
 		hbox2.add(speed);
 		hbox2.add(numThreads);
 		hbox2.add(problemSize);
 		vbox.add(hbox2);
 		
 		
 		Box hbox3 = Box.createHorizontalBox();
 		hbox3.add(startButton);
 		hbox3.add(randomCheckBox);
 		hbox3.add(randomDelayCheckBox);
 		vbox.add(hbox3);
 		
 		
 		frame.add(vbox, BorderLayout.NORTH);
 		frame.add(new JScrollPane(panel), BorderLayout.CENTER);
 		frame.setVisible(true);
 		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
 	}
 	/**
 	 * Creates label that visualizes the task
 	 * @param row
 	 * @param col
 	 * @param nums
 	 * @return
 	 */
 	private JLabel createLabel(int row, int col, int[] nums) {
 		final JLabel label = new JLabel(" "+Arrays.toString(nums));
 		label.setBounds(col*COL_WIDTH,row*ROW_HEIGHT+20,nums.length*COL_WIDTH,ROW_HEIGHT);
 		label.setBackground(COLOR_SCHEDULED);
 		label.setOpaque(true);
 		label.setToolTipText(Arrays.toString(nums));
 		label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 		threadSafe(new Runnable() {
 			public void run() {
 				panel.add(label);
 			}
 		});
 		return label;
 	}
 
 	/**
 	 * Sets color of the label
 	 * @param label
 	 * @param color
 	 * @return
 	 */
 	private  void setLabelColor(final JLabel label, final Color color) {
 		threadSafe(new Runnable() {
 			public void run() {
 				label.setBackground(color);
 			}
 		});
 	}
 
 	/**
 	 * Runs closure in Swing Event thread and repaints the panel. Sleeps after the change.
 	 * @param closure
 	 * @return
 	 */
 	private void threadSafe(Runnable r) {
 		try {
 			SwingUtilities.invokeAndWait(r);
 			panel.repaint();
 			if (randomDelayCheckBox.isSelected())
 			{
				Thread.sleep(Math.max(0, 1000-(int)(speed.getValue()*random.nextFloat()*2)));
 			}
 			else
 			{
 				Thread.sleep(1000-speed.getValue());
 			}
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		} 
 	}
 
 	/**
 	 * Splits a list of numbers in half
 	 */
 	private Map<Integer, int[]> split(int[] list) {
 		int listSize = list.length;
 		int middleIndex = listSize / 2;
 		Map<Integer, int[]> result = new HashMap<Integer, int[]>();
 		result.put(0, Arrays.copyOf(list, middleIndex));
 		result.put(1, Arrays.copyOfRange(list, middleIndex, list.length));
 		return result;
 	}
 
 	/**
 	 * Merges two sorted lists into one
 	 */
 	private int[] merge(JLabel label, int[] a, int[] b, int[] result) {
 		setLabelColor (label, threadColor());
 		int i = 0, j = 0, idx = 0;
 
 		while ((i < a.length) && (j < b.length)) {
 			if (a[i] <= b[j]) 
 			{
 				result[idx] = a[i++];
 			}
 			else 
 			{
 				result[idx] = b[j++];
 			}
 			idx++;
 		}
 
 		if (i < a.length) {
 			for (;i<a.length;i++)
 			{
 				result[idx++] = a[i];
 			}
 		}
 		else 
 		{
 			for (;j<b.length;j++)
 			{
 				result[idx++] = b[j];
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Returns color of current thread.
 	 */
 	private static Color threadColor() {
 		
 		return THREAD_COLORS[(threadNo() - 1) % THREAD_COLORS.length];
 	}
 
 	private static int threadNo() {
 		String name = Thread.currentThread().getName();
 		return Integer.valueOf(name.charAt(name.length()-1));
 	}
 
 
 	/**
 	 * Finishes the task
 	 * @param label
 	 * @param result
 	 * @return
 	 */
 	private int[] finishTask(final JLabel label, final int[] result) {
 		threadSafe(new Runnable() {
 			public void run() {
 				label.setText(Arrays.toString(result));
 				label.setBackground(COLOR_FINISHED);
 		}});
 		return result;
 	}
 	
 
 	
 	/**
 	 * Executes the demo.
 	 * @return
 	 */
 	private void runDemo() {
 		ForkJoinPool threadPool = new ForkJoinPool(numThreads.getValue());
 		int[] numbers = new int[problemSize.getValue()];
 
 		for (int i = 0; i < numbers.length; i++) {
 			if (randomCheckBox.isSelected())
 			{
 				numbers[i]=random.nextInt(100);
 			}
 			else
 			{
 				numbers[i]=problemSize.getValue()-i;
 			}
 		}
 		threadPool.invoke(new SortTask(numbers, 0, 0, createLabel(0, 0, numbers)));
 		
 		threadSafe(new Runnable() {
 			public void run() {
 				numThreads.setEnabled(true);
 				problemSize.setEnabled(true);
 				startButton.setEnabled(true);
 			}
 		});
 		System.out.println("Sorted numbers: "+Arrays.toString(numbers));
 	}
 	
 	private class SortTask extends RecursiveAction
 	{
 		
 		private static final long serialVersionUID = -686960611134519371L;
 		
 		private final int[] numbers;
 		private final int row;
 		private final int col;
 		private final JLabel label;
 		
 		public SortTask(int[] numbers, int row, int col, JLabel label) {
 			this.numbers = numbers;
 			this.row = row;
 			this.col = col;
 			this.label = label;
 		}
 		
 		@Override
 		protected void compute() {
 			System.out.println("Thread "+threadNo()+": Sorting "+Arrays.toString(numbers));
 			setLabelColor(label, threadColor());
 			switch (numbers.length) {
 			case 1:
 				finishTask(label, numbers);
 				return;
 			case 2:
 				if (numbers[0]>numbers[1])
 				{
 					int tmp = numbers[0];
 					numbers[0] = numbers[1];
 					numbers[1] = tmp;
 				}
 				finishTask(label, numbers);
 				return;
 			default:
 				Map<Integer, int[]> split = split(numbers);
 				int[] a = split.get(0);
 				int[] b = split.get(1);
 				JLabel label1 = createLabel(row+1, col, a);
 				JLabel label2 = createLabel(row+1, col + a.length, b);
 				setLabelColor(label, COLOR_WAIT);
 				invokeAll(new SortTask(a, row+1, col, label1), new SortTask(b, row+1, col + a.length, label2));
 				merge(label, a, b, numbers);
 				finishTask(label, numbers);
 			}
 		}
 		
 	}
 	
 	public static void  main(String[] args)
 	{
 		VisualForkJoinMergeSort demo = new VisualForkJoinMergeSort();
 		demo.start();
 	}
 }
 
