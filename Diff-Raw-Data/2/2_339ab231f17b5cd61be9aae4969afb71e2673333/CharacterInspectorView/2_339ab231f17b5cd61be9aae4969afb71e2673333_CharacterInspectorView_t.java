 package maro.example.sims;
 
 import maro.core.BBKeeper;
 import maro.wrapper.BBAffective;
 
 import java.util.Set;
 import java.util.HashMap;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JLabel;
 import java.awt.BorderLayout;
 import javax.swing.JTextArea;
 import javax.swing.BoxLayout;
 import javax.swing.JScrollPane;
 import javax.swing.GroupLayout;
 import javax.swing.BorderFactory;
 
 class CharacterInspectorView extends JFrame
 {
 	HashMap<String, JLabel> msj; // nameLabel X JLabel
     CharacterInspectorController cic;
 	JLabel hungry;
 	JLabel social;
 	JLabel cleaning;
 	JLabel energy;
 	JLabel lookFor;
 	JLabel day;
 	JLabel hour;
 	JLabel minute;
 	JLabel shiftOfDay;
 	JTextArea textArea;
 	PrintWriter pw;
 
     public CharacterInspectorView (String title) {
         super(title);
         this.cic = null;
         hungry = new JLabel();
         social = new JLabel();
         cleaning = new JLabel();
         energy = new JLabel();
         lookFor = new JLabel();
         day = new JLabel();
         hour = new JLabel();
         minute = new JLabel();
         shiftOfDay = new JLabel();
 		textArea = new JTextArea();
         msj = new HashMap<String, JLabel>();
 
 		String dump = System.getenv("dumpCharacterHistory");
 		if (dump != null && !dump.equals(title.toLowerCase()))
 			dump = null;
 
 		pw = null;
 		if (dump != null) {
 			try {
 				pw = new PrintWriter(new FileWriter("/tmp/"+dump+".txt", true));
 			} catch (Exception e) {
 				pw = null;
 			}
 		}
 
         initComponents();
 
 		pack();
     }
 
     public void setController(CharacterInspectorController cic) {
         this.cic = cic;
     }
 
     protected void initComponents() {
 		JPanel data = new JPanel();
 		data.setLayout(new BoxLayout(data,BoxLayout.X_AXIS) );
 		data.setBorder(BorderFactory.createTitledBorder("Data"));
 		getContentPane().add(data, BorderLayout.NORTH);
 
 		JPanel feelings = new JPanel();
 		//feelings.setBorder(BorderFactory.createTitledBorder("Feelings"));
 		GroupLayout gl = new GroupLayout(feelings);
 		gl.setAutoCreateGaps(true);
 		gl.setAutoCreateContainerGaps(true);
 		feelings.setLayout(gl);
 		data.add(feelings);
 
 		Set<String> ss = BBKeeper.getInstance().getEmotionType();
 
 		if (ss != null) {
 			for (String e : ss) {
 				JLabel j = new JLabel();
 				j.setText(e+": 0");
 				j.setName(e);
 				msj.put(e, j);
 			}
 		}
 
 		int number = 1;
 		GroupLayout.SequentialGroup sgh = gl.createSequentialGroup();
 		GroupLayout.ParallelGroup sghc1 = gl.createParallelGroup(GroupLayout.Alignment.LEADING);
 		GroupLayout.ParallelGroup sghc2 = gl.createParallelGroup(GroupLayout.Alignment.LEADING);
 		GroupLayout.ParallelGroup sghc3 = gl.createParallelGroup(GroupLayout.Alignment.LEADING);
 		GroupLayout.ParallelGroup sghc4 = gl.createParallelGroup(GroupLayout.Alignment.LEADING);
 		sgh.addGroup(sghc1).addGroup(sghc2).addGroup(sghc3).addGroup(sghc4);
 
 		GroupLayout.SequentialGroup sgv = gl.createSequentialGroup();
 		GroupLayout.ParallelGroup sgvl = null;
 
 		for (JLabel jl : msj.values()) {
 			int mod = number % 4;
 
 			switch (mod) {
 				case 1:
 					sgvl = gl.createParallelGroup(GroupLayout.Alignment.BASELINE);
 					sgv.addGroup(sgvl);
 					sgvl.addComponent(jl);
 					sghc1.addComponent(jl);
 					break;
 				case 2:
 					sgvl.addComponent(jl);
 					sghc2.addComponent(jl);
 					break;
 				case 3:
 					sgvl.addComponent(jl);
 					sghc3.addComponent(jl);
 					break;
 				default:
 					sgvl.addComponent(jl);
 					sgvl = null;
 					sghc4.addComponent(jl);
 					break;
 			}
 
 			number = number + 1;
 		}
 
         if (sgvl == null) {
             sgvl = gl.createParallelGroup(GroupLayout.Alignment.BASELINE);
             sgv.addGroup(sgvl);
         }
 
 		lookFor.setText("lookFor: ?");
 		lookFor.setName("lookFor");
 		sgvl.addComponent(lookFor);
 		sghc3.addComponent(lookFor);
 
 		day.setText("day: ?");
 		day.setName("day");
 		hour.setText("hour: ?");
 		hour.setName("hour");
 		minute.setText("minute: ?");
 		minute.setName("minute");
 		shiftOfDay.setText("shiftOfDay: ?");
 		shiftOfDay.setName("shiftOfDay");
 
         sgvl = gl.createParallelGroup(GroupLayout.Alignment.BASELINE);
         sgv.addGroup(sgvl);
         sgvl.addComponent(day);
         sghc1.addComponent(day);
 
         sgvl.addComponent(hour);
         sghc2.addComponent(hour);
 
         sgvl.addComponent(minute);
         sghc3.addComponent(minute);
 
         sgvl.addComponent(shiftOfDay);
         sghc4.addComponent(shiftOfDay);
 
         sgvl = gl.createParallelGroup(GroupLayout.Alignment.BASELINE);
         sgv.addGroup(sgvl);
 
 		cleaning.setText("cleaning: ?");
 		cleaning.setName("cleaning");
 		sgvl.addComponent(cleaning);
 		sghc1.addComponent(cleaning);
 
 		social.setText("social: ?");
 		social.setName("social");
 		sgvl.addComponent(social);
 		sghc2.addComponent(social);
 
 		energy.setText("energy: ?");
 		energy.setName("energy");
 		sgvl.addComponent(energy);
 		sghc3.addComponent(energy);
 
 		hungry.setText("hungry: ?");
 		hungry.setName("hungry");
 		sgvl.addComponent(hungry);
 		sghc4.addComponent(hungry);
 
 
 		gl.setHorizontalGroup( sgh );
 		gl.setVerticalGroup( sgv );
 
 		JPanel history = new JPanel();
 		history.setLayout(new BoxLayout(history,BoxLayout.X_AXIS) );
 		history.setBorder(BorderFactory.createTitledBorder("History"));
 		getContentPane().add(history, BorderLayout.SOUTH);
 
 		textArea.setColumns(20);
         textArea.setLineWrap(true);
         textArea.setRows(5);
         textArea.setWrapStyleWord(true);
         textArea.setEditable(false);
 
 		JScrollPane scroll = new JScrollPane(textArea);
 		history.add(scroll);
     }
 
 	private boolean firstLine = true;
     public void update() {
         if (cic == null) return ;
 
 		String csv = "";
 		if (firstLine == true) {
 			csv += "step";
 			for (String emotion : msj.keySet()) {
 				csv += "," + emotion;
 			}
 
			csv += ",hungry,social,cleaning,energy,orientation,weekday,day,hour,minute,second,shiftOfDay\n";
 			firstLine = false;
 		}
 
 		BBAffective bb = null;
         HouseModel.Agent a;
 
 		csv += cic.getParent().getStep();
         a = cic.getAgent();
         if (a != null)
             bb = BBKeeper.getInstance().get(a.getName());
 		for (String emotion : msj.keySet()) {
 			JLabel l = msj.get(emotion);
 			csv += ",";
 			if (bb != null) {
 				Integer v = bb.getEmotionPotence(emotion);
 				updateLabel(l, (v==null)?0:v);
 				csv += (v==null)?0:v.toString();
 			}
 		}
 
         if (a != null) {
             hungry.setText("hungry: "+a.getHungry());
             social.setText("social: "+a.getSocial());
             cleaning.setText("cleaning: "+a.getCleaning());
             energy.setText("energy: "+a.getEnergy());
             lookFor.setText("lookFor: "+a.getOrientationText());
             csv += "," + a.getHungry() + "," + a.getSocial() + "," + a.getCleaning()
                 + "," + a.getEnergy() + "," + a.getOrientationText();
         } else {
             hungry.setText("hungry: 0");
             social.setText("social: 0");
             cleaning.setText("cleaning: 0");
             energy.setText("energy: 0");
             lookFor.setText("lookFor: Unknow");
             csv += ",0,0,0,0,Unknow";
         }
 
 		Integer day  = cic.getParent().day;
 		Integer hour = cic.getParent().hour;
 		Integer mins = cic.getParent().mins;
 		Integer secs = cic.getParent().secs;
 		String shift = cic.getParent().shift;
 
 		this.day.setText("day: "+ (day+1));
 		this.hour.setText("hour: "+hour);
 		this.minute.setText("minute: "+mins);
 		this.shiftOfDay.setText("shift: "+shift);
 
 		csv += "," + cic.getParent().today + "," + day;
 		csv += "," + hour + "," + mins + "," + secs + "," + shift;
 
 		textArea.append(csv+"\n");
 		if (pw != null) {
 			pw.println(csv);
 			pw.flush();
 		}
     }
 
 	protected void updateLabel(javax.swing.JLabel label, Integer d) {
 		String s = label.getText().split(":")[1]; // pega o segundo
 		Integer val;
 
 		if (d == null) {
 			label.setText(label.getName() + ": 0");
         	label.setForeground(new java.awt.Color(0, 0, 0));
 			return ;
 		}
 
 		try {
 			val = Integer.parseInt(s.trim());
 		} catch (Exception e) {
 			val = 0;
 		}
 
 		if ( d > val )
         	label.setForeground(new java.awt.Color(0, 0, 255));
 		else if ( d < val )
         	label.setForeground(new java.awt.Color(255, 0, 0));
 		else
         	label.setForeground(new java.awt.Color(0, 0, 0));
 
 		label.setText(label.getName() + ": " + d);
 	}
 };
