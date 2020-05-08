 package pucp.wallace;
 
 /*
  * Nodo del UHTree.
  */
 public class UHNode extends IntegerHeap {
 	private volatile int min;
 	private final int order;
 	private final int shift;
 	private final int max;
 	private volatile UHList minElements;
 	private volatile boolean isEmpty;
 	private final IntegerHeap HQ;
 	private volatile UHNode[] LQ;
 	private volatile UHElement best;
 
 	/*
 	 * Crea un UHNode con indices en el rango [0, 2^2^order[. Order es un entero
 	 * entre 0 y 5 inclusive.
 	 */
 	public UHNode(int order) {
 		isEmpty = true;
 		this.order = order;
 		shift = 1 << (order - 1);
 		max = (int) ((1L << (1 << order)) - 1);
 		minElements = new UHList();
 		HQ = UHTreeCreator.getNewIntegerHeap(order - 1);
 		LQ = new UHNode[max + 1];
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see pucp.wallace.IntegerHeap#isEmpty()
 	 */
 	public boolean isEmpty() {
 		assert !isEmpty || isEmpty && best == null;
 		return isEmpty;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see pucp.wallace.IntegerHeap#getMinHash()
 	 */
 	public int getMinHash() {
 		assert isEmpty == false;
 		return min;
 	}
 
 	/*
 	 * Retorna la lista de elementos cuyos hash es el menor hash de este node.
 	 */
 	public UHList getMinElements() {
 		assert isEmpty == false;
 		return minElements;
 	}
 
 	/*
 	 * Comparador para dos enteros como si fueran unsigned.
 	 */
 	public static int compareUnsignedInt(int a, int b) {
 		long val = (a & 0xffffffffL) - (b & 0xffffffffL);
 		return val < 0 ? -1 : val > 0 ? 1 : 0;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see pucp.wallace.IntegerHeap#add(int, java.lang.Object)
 	 */
 	public void add(int x, Object value) {
 		UHNode node = this;
 		int H, L;
 		// Iterativamente recorre el arbol.
 		while (true) {
 			synchronized (node) {
 				if (node.isEmpty() || node.min == x) // Se actualiza el min y se
 														// finaliza en el nodo
 														// actual.
 				{
 					node.min = x;
 					node.minElements.addNewElement(value);
 					node.isEmpty = false;
 					return;
 				} else // Se hace la llamada al siguiente nivel.
 				{
 					if (compareUnsignedInt(x, node.min) < 0) // Hay un nuevo
 																// minimo. Los
 																// valores
 																// minimos
 																// anteriores
 																// seran
 																// insertados en
 																// un nivel
 																// siguiente.
 					{
 						int aux = node.min;
 						node.min = x;
 						x = aux;
 						Object aux2 = node.minElements;
 						node.minElements = new UHList(value);
 						value = aux2;
 					}
 					assert node.min != x;
 					H = x >>> node.shift;
 					L = x ^ (H << node.shift);
 					UHNode child = node.LQ[H];
 
 					if (child == null) {
 						node.HQ.add(H);
 						child = new UHNode(order - 1);
 						node.LQ[H] = child;
 					}
 					node = child;
 					x = L;
 				}
 			}
 		}
 	}
 
 	/*
 	 * Actualiza el elemento mas comun del arbol.
 	 */
 	public void updateBest(UHElement candidate) {
		UHElement b = best;
		if (b == null || candidate.getCount() > b.getCount()) best = candidate;
 	}
 
 	/*
 	 * Determina si un elemento existe en el arbol.
 	 */
 	public boolean contains(int x, Object value) {
 		UHNode node = this, parent = null;
 		int H, L;
 		// Realiza la busqueda en el arbol iterativamente.
 		while (true) {
			UHElement b = node.best;
			if (b != null) { synchronized (b) {
					if (b.equals(value)) // Si el elemento buscado es el
										 // mejor elemento del nodo
										 // actual.
 					{
						b.incr();
 						if (parent != null)
							parent.updateBest(b);
 						return true;
 					}
 				}
 			}
 			if (node.isEmpty()) {
 				return false;
 			} else if (node.min == x) // Si el elemento estaria en los elementos
 										// con hash minimo.
 			{
 				synchronized (node.minElements) {
 					return node.minElements.contains(value, this);
 				}
 			} else // Si el elemento estaria en un nivel inferior. Esta parte es
 					// lock-free.
 			{
 				H = x >>> node.shift;
 				L = x ^ (H << node.shift);
 				UHNode child = node.LQ[H];
 				if (child == null) {
 					return false;
 				} else {
 					parent = node;
 					node = child;
 					x = L;
 				}
 			}
 
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see pucp.wallace.IntegerHeap#remove(int, java.lang.Object)
 	 */
 	public boolean remove(int x, Object value) {
 		synchronized (this) {
 			if (best != null && best.equals(value)) // Si el elemento a eliminar
 													// es el best del nodo
 													// actual, se elimina.
 				best = null;
 
 			if (isEmpty())
 				return false;
 			int H, L;
 			if (min == x) // Si el elemento a eliminar estaria en la lista de
 							// minimos
 			{
 				boolean success;
 				if (value instanceof UHList) {
 					assert ((UHList) value).getSet().equals(
 							minElements.getSet());
 					minElements = new UHList();
 					best = null;
 					success = true;
 				} else {
 					success = minElements.remove(value);
 				}
 				if (!success) {
 					return false;
 				} else // Se elimino satisfactoriamente
 				{
 					if (!minElements.isEmpty()) // Ningun cambio adicional
 					{
 						return true;
 					} else // Se debe actualizar el nuevo menor, ya que el menor
 							// anterior desaparecio.
 					{
 						if (HQ.isEmpty()) {
 							isEmpty = true;
 							return true;
 						} else {
 							H = HQ.getMinHash();
 							UHNode child = LQ[H];
 							L = child.getMinHash();
 							// Se elimina value de los hijos.
 							value = minElements = child.getMinElements();
 							min = (H << shift) + L;
 						}
 					}
 				}
 			} else {
 				H = x >>> shift;
 				L = x ^ (H << shift);
 			}
 			// Se elimina el nuevo x.
 			boolean success = false;
 			UHNode child = LQ[H];
 			if (child != null) {
 				success = child.remove(L, value);
 				if (child.isEmpty()) {
 					LQ[H] = null;
 					HQ.remove(H, H);
 				}
 			}
 			return success;
 		}
 	}
 
 }
