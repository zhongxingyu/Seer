 import java.util.List;
 import java.util.ArrayList;
 
 
 public class Heap {
 private List<Integer> h = new ArrayList<Integer>();
 private int k;
 
 public Heap(int k) {
 this.k=k;
 }
 
 public void insert(int i) {
 h.add(i);
 int j = h.size()-1;
 if(j==0) {
 return;
 }
while(h.get(j)<h.get(((int) Math.floor((((double) i)-1)/k)))) {
int l = h.get(((int) Math.floor((((double) i)-1)/k)));
 int temp = h.get(j);
 h.set(j, h.get(l));
 h.set(l, temp);
 }
 }
 
 public int extractMin() {
 assert h.size()!=0;
 int min = h.get(0);
 if(h.size()==1) {
 h.remove(0);
 }
 else {
 h.set(0, h.get(h.size()-1));
 h.remove(h.size()-1);
 int done =0;
 int node=0;
 while(done==0) {
 int minchild=1;
 done=1;
 for(int j=1; j<=k; j++) {
 if((node*k+j)>=h.size()) {
 break;
 }
 if(h.get(node*k+j)<h.get(minchild)) {
 minchild=j;
 }
 }
 if((node*k+1)>=h.size()) {
 break;
 }
 if(h.get(node*k+minchild)>h.get(node)) {
 done=0;
 int temp = h.get(node);
 h.set(node, h.get(node*k+minchild));
 h.set(node*k+minchild, temp);
 }
 node=node*k+minchild;
 }
 }
 return min;
 }
 
 }
