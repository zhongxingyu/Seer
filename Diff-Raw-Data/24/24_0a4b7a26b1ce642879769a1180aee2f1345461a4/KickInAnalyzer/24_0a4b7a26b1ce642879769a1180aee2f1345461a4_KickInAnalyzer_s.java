 /*
  * $Log: KickInAnalyzer.java,v $
  * Revision 1.3  2003/01/04 08:35:36  koji
  * �ѥ���Ϣ�����ѥ��ߥ����ɥ�֥롢���줾�����ײ��Ϥ��ɲ�
  *
  * Revision 1.2  2002/10/15 10:09:48  koji
  * ��������Pass������ȡ�Team/�����̥ơ��֥��ɲ�
  *
  * Revision 1.1  2002/09/12 11:25:42  koji
  * ��ײ��ϡ�������ǽ���ɲá���������ե�����졼�����ե������б�
  *
  *
  */
 
 package soccerscope.util.analyze;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import com.jamesmurty.utils.XMLBuilder;
 
 import soccerscope.model.GameEvent;
 import soccerscope.model.PlayMode;
 import soccerscope.model.Scene;
 import soccerscope.model.Team;
 
 public class KickInAnalyzer extends SceneAnalyzer implements Xmling {
 
 	public static String NAME = "Kick in";
 
 	public String getName() {
 		return NAME;
 	}
 
 	private List<KickIn> kicksinList = new LinkedList<KickIn>();
 
 	public static class KickIn implements Xmling {
 		public int side;
 		public int time;
 
 		public KickIn(int side, int time) {
 			this.side = side;
 			this.time = time;
 		}
 
 		@Override
 		public void xmlElement(XMLBuilder builder) {
 			builder.elem("kickin")
			.attr("side", String.valueOf(this.side))
 			.attr("time", String.valueOf(this.time));
 		}
 	}
 
 	public GameEvent analyze(Scene scene, Scene prev) {
 		GameEvent ge = null;
 
 		/* ���å�����⡼�ɤϻ��郎�ߤޤ�ʤ����ᡢprev�򸫤ơ����å�����⡼�ɤˤʤä��ִ֤����򡢼����� */
 		if (prev != null) {
 			if (isPlayModeChanged(scene, prev, PlayMode.PM_KickIn_Left)) {
 				countUp(Team.LEFT_SIDE, scene.time);
 				ge = new GameEvent(scene.time, GameEvent.KICK_IN);
 			}
 			if (isPlayModeChanged(scene, prev, PlayMode.PM_KickIn_Right)) {
 				countUp(Team.RIGHT_SIDE, scene.time);
 				ge = new GameEvent(scene.time, GameEvent.KICK_IN);
 			}
 		}
 		return ge;
 	}
 
 	@Override
 	public void countUp(int side, int time) {
 		this.kicksinList.add(new KickIn(side, time));
 		if (side == Team.LEFT_SIDE)
 			this.countUpLeft(time);
 		if (side == Team.RIGHT_SIDE)
 			this.countUpRight(time);
 	}
 
 	@Override
 	public void xmlElement(XMLBuilder builder) {
 		XMLBuilder kicksin = builder.elem("kicksin").attr("left",
 				String.valueOf(this.lcount)).attr("right",
 				String.valueOf(this.rcount));
 
 		for (KickIn k : this.kicksinList) {
 			k.xmlElement(kicksin);
 		}
 
 	}
 }
