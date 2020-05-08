 /*
  * This file is part of SchedVis.
  * 
  * SchedVis is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SchedVis is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SchedVis. If not, see <http://www.gnu.org/licenses/>.
  */
 /**
  * 
  */
 package cz.muni.fi.spc.SchedVis.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Cursor;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 
 import cz.muni.fi.spc.SchedVis.Main;
 import cz.muni.fi.spc.SchedVis.background.LookAhead;
 import cz.muni.fi.spc.SchedVis.background.Player;
 import cz.muni.fi.spc.SchedVis.model.entities.Event;
 import cz.muni.fi.spc.SchedVis.model.entities.Machine;
 import cz.muni.fi.spc.SchedVis.model.models.TimelineSliderModel;
 
 /**
  * Implements a timeline "widget," used to move forward or backwards on the
  * timeline.
  * 
  * @author Lukáš Petrovický <petrovicky@mail.muni.cz>
  * 
  */
 public final class SliderPanel extends JPanel implements ChangeListener,
     ActionListener {
 
 	private static final long serialVersionUID = 6091479520934383104L;
 	private TimelineSliderModel tlsm = null;
 	private final JButton btnStart = new JButton("|<");
 	private final JButton btnEnd = new JButton(">|");
 	private final JButton btnPlay = new JButton("|>");
 	private final JButton btnPrev = new JButton("<<");
 	private final JButton btnNext = new JButton(">>");
 	private boolean playing = false;
 
 	/**
 	 * The constructor.
 	 */
 	public SliderPanel() {
 		this.setLayout(new BorderLayout());
 		// left-side buttons
 		final JPanel innerPane = new JPanel();
 		innerPane.setLayout(new FlowLayout());
 		this.btnStart.setEnabled(false);
 		this.btnPrev.setEnabled(false);
 		innerPane.add(this.btnStart);
 		innerPane.add(this.btnPrev);
 		// middle slider
 		this.add(innerPane, BorderLayout.LINE_START);
 		final TimelineSlider slider = new TimelineSlider();
 		this.tlsm = TimelineSliderModel.getInstance(this);
 		slider.setModel(this.tlsm);
 		this.add(slider, BorderLayout.CENTER);
 		// right-side buttons
 		final JPanel innerPane2 = new JPanel();
 		innerPane2.setLayout(new FlowLayout());
 		innerPane2.add(this.btnPlay);
 		innerPane2.add(this.btnNext);
 		innerPane2.add(this.btnEnd);
 		this.add(innerPane2, BorderLayout.LINE_END);
 		// add action listeners to buttons
 		final JButton[] buttons = new JButton[] { this.btnStart, this.btnEnd,
 		    this.btnNext, this.btnPrev, this.btnPlay };
 		for (final JButton b : buttons) {
 			b.addActionListener(this);
 		}
 	}
 
 	/**
 	 * Listens to actions on the timeline buttons.
 	 */
 	@Override
 	public void actionPerformed(final ActionEvent e) {
 		final Object src = e.getSource();
 		if (src.equals(this.btnEnd)) {
 			this.tlsm.setValue(this.tlsm.getMaximum());
 		} else if (src.equals(this.btnStart)) {
 			this.tlsm.setValue(this.tlsm.getMinimum());
 		} else if (src.equals(this.btnPlay)) {
 			this.playing = !this.playing;
 			this.btnPlay.removeAll();
 			if (this.playing) {
 				this.btnPlay.add(new JLabel("||"));
 			} else {
 				this.btnPlay.add(new JLabel("|>"));
 			}
 			this.btnPlay.updateUI();
 			Player.getInstance().toggleStatus();
 		} else if (src.equals(this.btnNext) || src.equals(this.btnPrev)) {
 			if (src.equals(this.btnPrev)) {
 				try {
 					this.tlsm.setValue(Event.getPrevious(this.tlsm.getValue())
 					    .getVirtualClock());
 				} catch (Exception ex) {
 					this.tlsm.setValue(Event.getFirst().getVirtualClock());
 				}
 			} else {
 				try {
 					this.tlsm.setValue(Event.getNext(this.tlsm.getValue())
 					    .getVirtualClock());
 				} catch (Exception ex) {
 					this.tlsm.setValue(Event.getLast().getVirtualClock());
 				}
 			}
 		}
 	}
 
 	/**
 	 * Listens to changes on the timeline slider.
 	 */
 	public void stateChanged(final ChangeEvent e) {
 		final Object src = e.getSource();
 		if (src.equals(this.tlsm)) {
			if (this.tlsm.getValue() <= 1) {
 				return;
 			}
 			Integer value = this.tlsm.getValue();
 			if (!Event.existsTick(value)) {
 				this.tlsm.setValue(Event.getPrevious(value).getVirtualClock());
 			}
 			if (value.equals(this.tlsm.getMinimum())) {
 				this.btnPrev.setEnabled(false);
 				this.btnStart.setEnabled(false);
 				this.btnPlay.setEnabled(true);
 				this.btnNext.setEnabled(true);
 				this.btnEnd.setEnabled(true);
 			} else if (value.equals(this.tlsm.getMaximum())) {
 				this.btnPrev.setEnabled(true);
 				this.btnStart.setEnabled(true);
 				this.btnPlay.setEnabled(false);
 				this.btnNext.setEnabled(false);
 				this.btnEnd.setEnabled(false);
 			} else {
 				this.btnPlay.setEnabled(true);
 				this.btnPrev.setEnabled(true);
 				this.btnStart.setEnabled(true);
 				this.btnNext.setEnabled(true);
 				this.btnEnd.setEnabled(true);
 			}
 			if (!this.tlsm.getValueIsAdjusting()) {
 				Main.getFrame().setCursor(
 				    Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 				ScheduleTree.getInstance().updateUI();
 				// update the detail pane based on the tree selection
 				try {
 					DefaultMutableTreeNode n = (DefaultMutableTreeNode) ScheduleTree
 					    .getInstance().getLastSelectedPathComponent();
 					if (n.getUserObject() instanceof Machine) {
 						Main.getFrame().updateDetail((Machine) n.getUserObject());
 					} else {
 						Main.getFrame().updateDetail(null);
 					}
 				} catch (NullPointerException ex) {
 					Main.getFrame().updateDetail(null);
 				}
 				DescriptionPane.getInstance().updateFrame(this.tlsm.getValue());
 				LookAhead.submit();
 				Main.getFrame().setCursor(
 				    Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 			}
 		}
 	}
 }
