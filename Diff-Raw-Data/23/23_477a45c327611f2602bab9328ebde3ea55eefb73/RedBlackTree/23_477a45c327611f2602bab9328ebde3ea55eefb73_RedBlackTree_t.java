 package ru.spbstu.telematics.stu.collections;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 /**
  * Класс реализующий красно-черное дерево на основе 
  * интерфейса {@link IRedBlackTree}
  * @author simonenko 
  * @version 2.0
  */
 public class RedBlackTree<T extends Comparable<T>> implements IRedBlackTree<T>, Iterable<T>, Iterator<T> {
 
 	/**
 	 * Перечисление цветов узла дерева.
 	 */
 	enum NodeColor {
 		RED,
 		BLACK,
 		NONE
 	}
 	
 	/**
 	 * Класс реализующий узел дерева.
 	 */
 	public class Node {
 		
 		/**
 		 * Значение узла дерева.
 		 */
 		private T _value;
 		/**
 		 * Цвет узла.
 		 */
 		private NodeColor _color;
 		/**
 		 * Родительский узел.
 		 */
 		private Node _parent;
 		/**
 		 * Левый дочерниый узел.
 		 */
 		private Node _left;
 		/**
 		 * Правый дочерний узел.
 		 */
 		private Node _right;
 		
 		/**
 		 * Конструктор по-умолчанию.
 		 */
 		public Node() {
 			_value = null;
 			_color = NodeColor.NONE;
 			_parent = null;
 			_left = null;
 			_right = null;
 		}
 		
 		/**
 		 * Конструктор с параметрами, позволящими задать цвет и 
 		 * значение узла.
 		 * @param value - значение, которое будет сохранено в узле.
 		 * @param color - цвет узла.
 		 */
 		public Node(T value, NodeColor color) {
 			_value = value;
 			_color = color;
 			_parent = _nil;
 			_left = _nil;
 			_right = _nil;
 		}
 		
 		/**
 		 * Конструктор копий.
 		 * @param node - другой узел.
 		 */
 		public Node(Node node) {
 			_value = node._value;
 			_color = node._color;
 			_parent = node._parent;
 			_left = node._left;
 			_right = node._right;
 		}
 		
 		public boolean isFree() {
 			return _value == null || _value == _nil;
 		}
 		
 		public boolean isLeftFree() {
 			return _left == null || _left == _nil;
 		}
 		
 		public boolean isRightFree() {
 			return _right == null || _right == _nil;
 		}
 		
 		public boolean isParentFree() {
 			return _parent == null || _parent == _nil;
 		}
 		
 		public T getValue() {
 			return _value;
 		}
 
 		public void setValue(T value) {
 			_value = value;
 		}
 
 		public Node getParent() {
 			return _parent;
 		}
 		
 		public void setParent(Node node) {
 			_parent = node;
 		}
 		
 		public Node getLeft() {
 			return _left;
 		}
 		
 		public void setLeft(Node node) {
 			_left = node;
 			if(_left != null && _left != _nil) _left._parent = this;
 		}
 		
 		public Node getRight() {
 			return _right;
 		}
 
 		public void setRight(Node node) {
 			_right = node;
 			if(_right != null && _right != _nil) _right._parent = this;
 		}
 		
 		public boolean isBlack() {
 			return _color == NodeColor.BLACK;
 		}
 		
 		public void makeBlack() {
 			_color = NodeColor.BLACK;
 		}
 		
 		public boolean isRed() {
 			return _color == NodeColor.RED;
 		}
 		
 		public void makeRed() {
 			_color = NodeColor.RED;
 		}
 		
 		public NodeColor getColor() {
 			return _color;
 		}
 		
 		public void setColor(NodeColor color) {
 			_color = color;
 		}
 		
 		/**
 		 * Возвращет "дедушку" узла дерева.
 		 */
 		public Node getGrandfather() {
 			if(_parent != null && _parent != _nil)
 				return _parent._parent;
 			return null;
 		}
 		
 		/**
 		 * Возвращает "дядю" узла дерева.
 		 */
 		public Node getUncle() {
 			Node grand = getGrandfather();
 			if(grand != null)
 			{
 				if(grand._left == _parent)
 					return grand._right;
 				else if(grand._right == _parent)
 					return grand._left;
 			}
 			return null;	
 		}
 		
 		/**
 		 * Возвращает следующий по значению узел дерева.
 		 */
 		public Node getSuccessor()
 		{
 			Node temp = null;
 			Node node = this;
 			if(!node.isRightFree()) {
 				temp = node.getRight();
 				while(!temp.isLeftFree())
 					temp = temp.getLeft();
 				return temp;
 			}
 			temp = node.getParent();
 			while(temp != _nil && node == temp.getRight()) {
 				node = temp;
 				temp = temp.getParent();
 			}
 			return temp;
 		}
 		
 		public String getColorName() {
 			return ((isBlack()) ? "B" : "R");
 		}
 		
 	}
 	
 	/**
 	 * Корень дерева.
 	 */
 	private Node _root;
 	/**
 	 * Ограничитель, который обозначает нулевую ссылку.
 	 */
 	private Node _nil;
 	
 	/**
 	 * Ссылка на элемент на который указывает итератор.
 	 */
 	private Node _current;
 	
 	/**
 	 * Флаг удаления элемента через итератор, необходимый для того, чтобы
 	 * корректно работали {@link Iterator#hasNext()} и {@link Iterator#next()}
 	 */
 	private boolean _isRemoved;
 	
 	/**
 	 * Конструктор по-умолчанию.
 	 */
 	public RedBlackTree() {
 		_root = new Node();
 		_nil = new Node();
 		_nil._color = NodeColor.BLACK;
 		_root._parent = _nil;
 		_root._right = _nil;
 		_root._left = _nil;
		_isRemoved = false;
 	}
 	
 	/**
 	 * Статический метод, осуществляюший левый поворот дерева tree относительно узла node.
 	 * @param tree - дерево.
 	 * @param node - узел, относительно которого осущетвляется левый поворот.
 	 */
 	private static <T extends Comparable<T>> void leftRotate(RedBlackTree<T> tree, RedBlackTree<T>.Node node) {
 		RedBlackTree<T>.Node nodeParent = node.getParent();
 		RedBlackTree<T>.Node nodeRight = node.getRight();
 		if(nodeParent != tree._nil) {
 			if(nodeParent.getLeft() == node)
 				nodeParent.setLeft(nodeRight);
 			else
 				nodeParent.setRight(nodeRight);
 		}
 		else {
 			tree._root = nodeRight;
 			tree._root.setParent(tree._nil);
 		}
 		node.setRight(nodeRight.getLeft());
 		nodeRight.setLeft(node);
 	}
 	
 	/**
 	 * Статический метод, осуществляюший правый поворот дерева tree относительно узла node.
 	 * @param tree - дерево.
 	 * @param node - узел, относительно которого осущетвляется правый поворот.
 	 */
 	private static <T extends Comparable<T>> void rightRotate(RedBlackTree<T> tree, RedBlackTree<T>.Node node) {
 		RedBlackTree<T>.Node nodeParent = node.getParent();
 		RedBlackTree<T>.Node nodeLeft = node.getLeft();
 		if(nodeParent != tree._nil) {
 			if(nodeParent.getLeft() == node)
 				nodeParent.setLeft(nodeLeft);
 			else
 				nodeParent.setRight(nodeLeft);
 		}
 		else {
 			tree._root = nodeLeft;
 			tree._root.setParent(tree._nil);
 		}
 		node.setLeft(nodeLeft.getRight());
 		nodeLeft.setRight(node);
 	}
 	
 	/**
 	 * Печать дерева.
 	 * @param tree - дерево.
 	 */
 	public static <T extends Comparable<T>> void printTree(RedBlackTree<T> tree) {
 		ArrayList<RedBlackTree<T>.Node> nodes = new ArrayList<RedBlackTree<T>.Node>();
 		nodes.add(0, tree._root);
 		printNodes(tree, nodes);
 	}
 	
 	/**
 	 * Печать информации об узле дерева.
 	 * @param tree - ссылка на дерево.
 	 * @param nodes - список узлов на уровне дерева.
 	 */
 	private static <T extends Comparable<T>> void printNodes(RedBlackTree<T> tree, ArrayList<RedBlackTree<T>.Node> nodes) {
 		int childsCounter = 0;
 		int nodesAmount = nodes.size();
 		ArrayList<RedBlackTree<T>.Node> childs = new ArrayList<RedBlackTree<T>.Node>();
 		
 		for(int i = 0; i < nodesAmount; i++) {
 			if(nodes.get(i) != null && nodes.get(i) != tree._nil) {
 				System.out.print("(" + nodes.get(i).getValue().toString() + "," + nodes.get(i).getColorName() + ")");
 				if(!nodes.get(i).isLeftFree()) {
 					childs.add(nodes.get(i).getLeft());
 					childsCounter++;
 				}
 				else
 					childs.add(null);
 				if(!nodes.get(i).isRightFree()) {
 					childs.add(nodes.get(i).getRight());
 					childsCounter++;
 				}
 				else
 					childs.add(null);
 			}
 			else {
 				System.out.print("(nil)");
 			}
 		}
 		System.out.print("\n");
 		
 		if(childsCounter != 0)
 			printNodes(tree, childs);
 	}
 	
 	/**
 	 * Реализация метода добавления элемента дарева. На основе добавляемого значения
 	 * создается узел дерева типа {@link Node} красного цвета.
 	 * @param o - значение типа {@link Comparable} для вставки в дерево.
 	 */
 	@Override
 	public void add(T o) {
 		Node node = _root, temp = _nil;
 		Node newNode = new Node((T)o, NodeColor.RED);
 		while(node != null && node != _nil && !node.isFree()) {
 			temp = node;
 			if(newNode.getValue().compareTo(node.getValue()) < 0)
 				node = node.getLeft();
 			else
 				node = node.getRight();
 		}
 		newNode.setParent(temp);
 		if(temp == _nil)
 			_root.setValue(newNode.getValue());
 		else {
 			if(newNode.getValue().compareTo(temp.getValue()) < 0)
 				temp.setLeft(newNode);
 			else
 				temp.setRight(newNode);
 		}
 		newNode.setLeft(_nil);
 		newNode.setRight(_nil);
 		fixInsert(newNode);
 	}
 	
 	/**
 	 * Исправление древа для сохранения свойств красно-черного дерева.
 	 * @param node - добавленный узел.
 	 */
 	private void fixInsert(Node node) {
 		Node temp;
 		while(!node.isParentFree() && node.getParent().isRed()) {
 			if(node.getParent() == node.getGrandfather().getLeft()) {
 				temp = node.getGrandfather().getRight();
 				if(temp.isRed()) {
 					temp.makeBlack();
 					node.getParent().makeBlack();
 					node.getGrandfather().makeRed();
 					node = node.getGrandfather();
 				}
 				else {
 					if(node == node.getParent().getRight()) {
 						node = node.getParent();
 						leftRotate(this, node);
 					}
 					node.getParent().makeBlack();
 					node.getGrandfather().makeRed();
 					rightRotate(this, node.getGrandfather());
 				}
 			}
 			else {
 				temp = node.getGrandfather().getLeft();
 				if(temp.isRed()) {
 					temp.makeBlack();
 					node.getParent().makeBlack();
 					node.getGrandfather().makeRed();
 					node = node.getGrandfather();
 				}
 				else {
 					if(node == node.getParent().getLeft()) {
 						node = node.getParent();
 						rightRotate(this, node);
 					}
 					node.getParent().makeBlack();
 					node.getGrandfather().makeRed();
 					leftRotate(this, node.getGrandfather());
 				}
 			}
 		}
 		_root.makeBlack();
 	}
 	
 	/**
 	 * Реализация удаления элемента дерева.
 	 * @param o - значение типа {@link Comparable} для удаления из дерева.
 	 * @return true - если элемент был удален;
 	 * false - если элемента в дереве нет и удаление его невозможно.
 	 */
 	@Override
 	public boolean remove(T o) {
 		return remove(findNode(o));
 	}
 	
 	/**
 	 * 
 	 */
 	private boolean remove(Node node)
 	{
 		Node temp = _nil, successor = _nil;
 
 		if(node == null || node == _nil) 
 			return false;
 
 		if(node.isLeftFree() || node.isRightFree())
 			successor = node;
 		else
 			successor = node.getSuccessor();
 
 		if(!successor.isLeftFree())
 			temp = successor.getLeft();
 		else
 			temp = successor.getRight();
 		temp.setParent(successor.getParent());
 
 		if(successor.isParentFree())
 			_root = temp;
 		else if(successor == successor.getParent().getLeft())
 			successor.getParent().setLeft(temp);
 		else
 			successor.getParent().setRight(temp);
 
 		if(successor != node) {
 			node.setValue(successor.getValue());
 		}
 		if(successor.isBlack())
 			fixRemove(temp);
 		return true;
 	}
 	
 	/**
 	 * Исправляет дерево после удаления элемента для сохранения
 	 * красно-черных свойств дерева.
 	 * @param node - значение относительно которого необходимо производить
 	 * исправление дерева.
 	 */
 	private void fixRemove(Node node)
 	{
 		Node temp;
 		while(node != _root && node.isBlack()) {
 			if(node == node.getParent().getLeft()) {
 				temp = node.getParent().getRight();
 				if(temp.isRed()) {
 					temp.makeBlack();
 					node.getParent().makeRed();
 					leftRotate(this, node.getParent());
 					temp = node.getParent().getRight();
 				}
 				if(temp.getLeft().isBlack() && temp.getRight().isBlack()) {
 					temp.makeRed();
 					node = node.getParent();
 				}
 				else {
 					if(temp.getRight().isBlack()) {
 						temp.getLeft().makeBlack();
 						temp.makeRed();
 						rightRotate(this, temp);
 						temp = node.getParent().getRight();
 					}
 					temp.setColor(node.getParent().getColor());
 					node.getParent().makeBlack();
 					temp.getRight().makeBlack();
 					leftRotate(this, node.getParent());
 					node = _root;
 				}
 			}
 			else {
 				temp = node.getParent().getLeft();
 				if(temp.isRed()) {
 					temp.makeBlack();
 					node.getParent().makeRed();
 					rightRotate(this, node.getParent());
 					temp = node.getParent().getLeft();
 				}
 				if(temp.getLeft().isBlack() && temp.getRight().isBlack()) {
 					temp.makeRed();
 					node = node.getParent();
 				}
 				else {
 					if(temp.getLeft().isBlack()) {
 						temp.getRight().makeBlack();
 						temp.makeRed();
 						leftRotate(this, temp);
 						temp = node.getParent().getLeft();
 					}
 					temp.setColor(node.getParent().getColor());
 					node.getParent().makeBlack();
 					temp.getLeft().makeBlack();
 					rightRotate(this, node.getParent());
 					node = _root;
 				}
 			}
 		}
 		node.makeBlack();
 	}
 
 	/**
 	 * Реализует функцию проверки на содержание элемента в дереве.
 	 * @param o - значение типа {@link Comparable} для поиска в дерева.
 	 * @return true - если элемент найден; false - если элемент не найда.
 	 */
 	@Override
 	public boolean contains(T o) {
 		return (findNode(o) != _nil);
 	}
 	
 	/**
 	 * Поиск узла дерева со значением o.
 	 * @param o - значение типа {@link Comparable} для поиска в дерева.
 	 * @return узел дерева; если не найден - возвращает {@value #_nil}
 	 */
 	private Node findNode(T o) {
 		Node node = _root;
 		while(node != null && node != _nil && node.getValue().compareTo(o) != 0) {
 			if(node.getValue().compareTo(o) > 0)
 				node = node.getLeft();
 			else
 				node = node.getRight();
 		}
 		return node;
 	}
 
	/**
	 * Метод для получения первого(наименьшего) элемента структуры.
	 * @return наименьший элемент дерева
	 */
 	private Node first()
 	{
 		Node node = _root;
 		while(node.getLeft() != null && node.getLeft() != _nil) {
 			if(!node.isLeftFree()) 
 				node = node.getLeft();
 		}
 		return node;
 	}
 	
 	@Override
 	public Iterator<T> iterator() {
 		_current = null;
		_isRemoved = false;
 		return this;
 	}
 
 	@Override
 	public boolean hasNext() {
 		if(_current != null) {
 			if(!_isRemoved) {
 				RedBlackTree<T>.Node node = _current.getSuccessor();
 				return (node != null && node != _nil);
 			}
 			return (_current != null && _current != _nil);
 		}
 		else {
 			return (_root != null && !_root.isFree());
 		}
 	}
 
 	@Override
 	public T next() {
 		if(_current != null) {
 			if(!_isRemoved)
 				_current = _current.getSuccessor();
 			else
 				_isRemoved = false;
 		}
 		else {
 			_current = first();
 		}
 		if(_current == null || _current == _nil)
 			throw new NoSuchElementException();
 		return _current.getValue();
 	}
 
 	@Override
 	public void remove() {
 		if(_current != null && !_isRemoved) {
 			remove(_current);
 			_isRemoved = true;
 		}
 		else
 			throw new IllegalStateException();
 	}
 }
