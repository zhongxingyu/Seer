 package data.tasks;
 
 import java.io.*;
 import java.util.*;
 import java.util.Map.Entry;
 
 import javax.swing.JOptionPane;
 
 import data.task.Task;
 
 import static data.StaticData.*;
 
 /**
  * 񼯺ϵĸ
  * 
  * @author lqy
  * 
  * @param <E>
  *            ϴ洢
  * @param <K>
  *            ϵһ
  */
 public abstract class TaskMap<E extends Task, K extends Task> {
 	public static final String DATA = DATAFOLDER; // 񼯺ϵĴļ
 
 	/**
 	 * 񼯺
 	 */
 	protected TreeMap<String, E> tasks;
 	/**
 	 * 񼯺ϵһ񼯺
 	 */
 	protected TaskMap<K, ? extends Task> father;
 	/**
 	 * ļļ<br>
 	 * {@link #readTasks()}ʱmkdirs{@link #writeTasks()} дȥ<br>
 	 * {@link #path}ʱҲһdir
 	 */
 	protected String dir;
 	/**
 	 * ļ·
 	 */
 	protected String path;
 
 	/**
 	 * TaskMapûе,ԿΪ,ֻҪ{@linkplain #dir}
 	 * 
 	 * @param dir
 	 */
 	protected abstract void buildDir(String dir);
 
 	/**
 	 * ͬһ񼯺תδɵ
 	 * 
 	 * @param last
 	 *            ͬһ񼯺
 	 */
 	protected void bringLast(TaskMap<E, K> last) {
 		Iterator<Entry<String, E>> it = last.iterator();
 		E d;
 		while (it.hasNext()) {
 			d = it.next().getValue();
			if (!d.finished)
				if (d.lastTime == d.needTime)
					d.finished = true;
				else {
					tasks.put(d.info, d);
					it.remove();
				}
 		}
 		this.writeTasks();// ѽ޸д
 		last.writeTasks();// ޸д
 	}
 
 	/**
 	 * 񼯺
 	 * 
 	 * @return ļڻ쳣򷵻false
 	 */
 	@SuppressWarnings("unchecked")
 	public boolean readTasks() {
 		// ļĸļв򴴽
 		File d = new File(dir);
 		if (!d.exists()) {
 			d.mkdirs();
 			return false;
 		}
 
 		// ļ򷵻
 		File f = new File(path);
 		if (!f.exists())
 			return false;
 
 		try {
 			ObjectInputStream in = new ObjectInputStream(
 					new BufferedInputStream(new FileInputStream(f)));
 			tasks = (TreeMap<String, E>) in.readObject();
 			in.close();
 			return true;
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	/**
 	 * д񼯺
 	 */
 	public void writeTasks() {
 		try {
 			ObjectOutputStream out = new ObjectOutputStream(
 					new BufferedOutputStream(new FileOutputStream(path)));
 			out.writeObject(tasks);
 			out.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * 񼯺ĸ<br>
 	 * ַӦÿֽ׶񼯺϶Ӧ߱
 	 * 
 	 * @param incFinished
 	 *            ɵ?
 	 * @return
 	 */
 	public int getSize(boolean incFinished) {
 		if (incFinished)
 			return tasks.size();
 
 		int size = 0;
 		Iterator<Entry<String, E>> it = tasks.entrySet().iterator();
 		while (it.hasNext())
 			if (!it.next().getValue().finished)
 				size++;
 		return size;
 	}
 
 	/**
 	 * 񼯺ϵĵ
 	 * 
 	 * @return
 	 */
 	public Iterator<Entry<String, E>> iterator() {
 		return tasks.entrySet().iterator();
 	}
 
 	/**
 	 * Ƚc
 	 * 
 	 * @param c
 	 *            Ƚ,<b>Ϊnull򷵻Ĭϵĵ</b>
 	 * @return õlistĵ
 	 */
 	public Iterator<E> iterator(Comparator<Task> c) {
 		if (c == null)
 			return tasks.values().iterator();
 
 		LinkedList<E> list = new LinkedList<E>(tasks.values());
 		Collections.sort(list, c);
 		return list.iterator();
 	}
 
 	/**
 	 * ȡ
 	 * 
 	 * @param info
 	 *            
 	 * @return һ,һ,һܵ...
 	 */
 	public E get(Object info) {
 		return tasks.get(info);
 	}
 
 	/**
 	 * 
 	 * 
 	 * @param e
 	 *            Ҫӵ
 	 */
 	public boolean add(E e) {
 		if (tasks.get(e.info) == null) {
 			tasks.put(e.info, e);
 			this.writeTasks(); // ޸,޸
 			return true;
 		}
 		JOptionPane.showMessageDialog(null, "" + e.info + "Ѵ", "ܾ",
 				JOptionPane.ERROR_MESSAGE);
 		return false;
 	}
 
 	/**
 	 * ɾ
 	 * 
 	 * @param info
 	 *            Ҫɾ
 	 */
 	public void remove(String info) {
 		tasks.remove(info);
 		this.writeTasks();// ޸,޸
 	}
 
 	/**
 	 * ޸һ
 	 * 
 	 * @param origin
 	 *            ԭ
 	 * @param now
 	 *            ڵ
 	 */
 	public void modify(String origin, E now) {
 		remove(origin);
 		add(now);
 	}
 
 	/**
 	 * Ƿɱ༭(ɾ,޸)
 	 * 
 	 * @param info
 	 *            
 	 * @return ɱ༭true,򷵻false
 	 */
 	public boolean isTaskEditable(String info) {
 		E task = tasks.get(info);
 		return task.needTime == 0L;
 	}
 
 	/**
 	 * չinfoĵʱ
 	 * 
 	 * @param info
 	 *            һַ
 	 * @param time
 	 *            Ҫӵʱ(Ϊʱͱ˼ٵʱ)
 	 */
 	public void addNeedTime(String info, long time) {
 		String up = null; // ϼַ
 		E task = tasks.get(info);
 		if (task != null) {
 			up = task.father;
 			task.needTime += time;
 			task.finished = false;
 			this.writeTasks(); // ˼ѱ޸
 		} else
 			up = info;
 
 		if (up != null)
 			father.addNeedTime(up, time);
 	}
 
 	/**
 	 * infoĳʱ
 	 * 
 	 * @param info
 	 *            һַ
 	 * @param time
 	 *            Ҫӵʱ(Ϊʱͱ˼ٵʱ)
 	 */
 	public void addLastTime(String info, long time) {
 		String up = null; // ϼַ
 		E task = tasks.get(info);
 		if (task != null) {
 			up = task.father;
 			task.lastTime += time;
 			this.writeTasks(); // ˼ѱ޸
 		} else
 			up = info;
 
 		if (up != null)
 			father.addLastTime(up, time);
 	}
 
 	/**
 	 * ش˼ϵпܵϼļ
 	 * 
 	 * @return
 	 */
 	public LinkedList<String> getFathers() {
 		LinkedList<String> ans = new LinkedList<String>();
 		if (father != null) {
 			for (K e : father.tasks.values())
 				ans.add(e.info);
 			ans.addAll(father.getFathers());
 		}
 		return ans;
 	}
 
 	/**
 	 * һΪ,ҵΧ׶ε񼯺(ͬһ񼯺ϷΧ)
 	 * <p>
 	 * :calΪ2012/1/1,ôһMonthgetBrothers(cal) ᷵<br>
 	 * "1"-2012/1/1 "2"-2012/2/1 ...
 	 * 
 	 * @param cal
 	 *            ڶ
 	 * @return
 	 */
 	public abstract TreeMap<String, Calendar> getBrothers(Calendar cal);
 
 	/**
 	 * ڶΪԴбе
 	 * <p>
 	 * EditerDialogĹ췽õ, ͬʱgetBrothersҲ
 	 * 
 	 * @param cal
 	 *            ڶ
 	 * @return
 	 */
 	public abstract String getItemByCal(Calendar cal);
 
 	/**
 	 * һΪ,õǰĸҪ(ʾtablePaneı)
 	 * 
 	 * @param cal
 	 * @return
 	 */
 	public abstract String getPanelBorder(Calendar cal);
 }
