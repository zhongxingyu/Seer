 public class Solution {
 	public int ladderLength(String start, String end, HashSet<String> dict) {
 		// Start typing your Java solution below
 		// DO NOT write main() function
 		class QueueNode {
 			public String data;
 			public int step;
 			public QueueNode(String str, int n) {
 				data = str;
 				step = n;
 			}
 		}
 		if (start.equals(end))
 			return 0;
 		HashSet<String> visited = new HashSet<String>();
 		visited.add(start);
 		Queue<QueueNode> queue = new LinkedList<QueueNode>();
 		queue.add(new QueueNode(start, 1));
 		while (!queue.isEmpty()) {
 			QueueNode cur = queue.poll();
 			if( cur.data.equals(end) )
 				return cur.step;
 			StringBuilder sb = null;
 			for( int i = 0; i < cur.data.length(); ++i ) {
 				for( int j = 0; j < 26; ++j ) {
 					char ch = (char)('a' + j);
 					sb = new StringBuilder(cur.data);
 					sb.setCharAt(i, ch);
 					String candidate = sb.toString();
					if (candidate.equal(end))
						return cur.step + 1;
 					if (dict.contains(candidate) ) {
 						if (visited.contains(sb.toString()))
 							continue;
 						QueueNode node = new QueueNode(candidate, cur.step + 1);
 						queue.add(node);
 						visited.add(candidate);
 					}
 				}
 			}
 		}
 		return 0;
 	}
 }
