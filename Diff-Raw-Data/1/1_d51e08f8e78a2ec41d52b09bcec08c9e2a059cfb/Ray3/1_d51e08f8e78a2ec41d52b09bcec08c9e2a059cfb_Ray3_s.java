 package de.widemeadows.projectcore.math;
 
 import de.widemeadows.projectcore.cache.IObjectCache;
 import de.widemeadows.projectcore.cache.ObjectFactory;
 import de.widemeadows.projectcore.cache.ThreadLocalObjectCache;
 import de.widemeadows.projectcore.cache.annotations.ReturnsCachedValue;
 import org.jetbrains.annotations.NotNull;
 
 /**
  * Strahl im 3D-Raum, bestehend aus projectPointg und Richtung
  * @see RayFactory
  */
 public final class Ray3 {
 
 	/**
 	 * Instanz, die die Verwaltung nicht länger benötigter Instanzen übernimmt
 	 */
 	public static final IObjectCache<Ray3> Cache = new ThreadLocalObjectCache<Ray3>(new ObjectFactory<Ray3>() {
 		@NotNull
 		@Override
 		public Ray3 createNew() {
 			return new Ray3();
 		}
 	});
 
 	/**
 	 * Erzeugt eine neue Ray-Instanz.
 	 * <p>
 	 * <strong>Hinweis:</strong> Der Zustand des Rays kann korrupt sein!
 	 * </p>
 	 *
 	 * @param originX Der Ursprung (X-Komponente)
 	 * @param originY Der Ursprung (Y-Komponente)
 	 * @param originZ Der Ursprung (Z-Komponente)
 	 * @param directionX Die RiprojectPoint(X-Komponente)
 	 * @param directionY Die Richtung (Y-Komponente)
 	 * @param directionZ Die Richtung (Z-Komponente)
 	 * @return Der neue oder aufbereitete Vektor
 	 * @see #Cache
 	 */
 	@NotNull
 	public static Ray3 createNew(final float originX, final float originY, final float originZ,
 	                      final float directionX, final float directionY, final float directionZ) {
 		return Cache.getOrCreate().set(originX, originY, originZ, directionX, directionY, directionZ);
 	}
 
 	/**
 	 * Erzeugt eine neue {@link Ray3}-Instanz.
 	 * <p>
 	 * <strong>Hinweis:</strong> Der Zustand kann korrupt sein!
 	 * </p>
 	 *
 	 * @param origin Der Ursprung
 	 * @param direction Die Richtung
 	 * @return Der neue oder aufbereitete Ray
 	 * @see #Cache
 	 */
 	public static Ray3 createNew(@NotNull final Vector3 origin, @NotNull final Vector3 direction) {
 		return Cache.getOrCreate().set(origin, direction);
 	}
 
 	/**
 	 * Erzeugt eine neue {@link Ray3}-Instanz.
 	 * <p>
 	 * <strong>Hinweis:</strong> Der Zustand kann korrupt sein!
 	 * </p>
 	 *
 	 * @param other    Der zu kopierende Ray
 	 * @return Der neue oder aufbereitete Ray
 	 * @see #Cache
 	 */
 	public static Ray3 createNew(@NotNull final Ray3 other) {
 		return Cache.getOrCreate().set(other);
 	}
 
 	/**
 	 * Erzeugt eine neue {@link Ray3}-Instanz.
 	 * <p>
 	 * <strong>Hinweis:</strong> Der Zustand kann korrupt sein!
 	 * </p>
 	 *
 	 * @return Der neue oder aufbereitete Ray
 	 * @see #Cache
 	 */
 	@NotNull
 	public static Ray3 createNew() {
 		return Cache.getOrCreate();
 	}
 
 	/**
 	 * Recyclet einen Ray
 	 *
 	 * @param box Der zu recyclende Ray
 	 * @see #Cache
 	 * @see AxisAlignedBox#recycle()
 	 */
 	public static void recycle(@NotNull final Ray3 box) {
 		Cache.registerElement(box);
 	}
 
 	/**
 	 * Registriert diesen Ray für das spätere Cache
 	 *
 	 * @see #Cache
 	 * @see Vector3#recycle(Vector3)
 	 */
 	public void recycle() {
 		Cache.registerElement(this);
 	}
 
 	/**
 	 * Der Ursprung des Strahls
 	 */
 	@NotNull
 	public final Vector3 origin = Vector3.createNew();
 
 	/**
 	 * Die Richtung des Strahls
 	 *
 	 * <h3>Vektor nicht manuell setzen!</h3>
 	 * Der Vektor muss über die Methode {@link #setDirection(Vector3)} oder {@link #setDirection(float, float, float)}
 	 * gesetzt werden!
 	 *
 	 * @see #invDirection
 	 * @see #setDirection(Vector3)
 	 * @see #setDirection(float, float, float)
 	 */
 	@NotNull
 	public final Vector3 direction = Vector3.createNew(0.57735f, 0.57735f, 0.57735f);
 
 	/**
 	 * Die reziproke Richtung des Strahls
 	 * @see #direction
 	 * @see #setDirection(Vector3)
 	 * @see #setDirection(float, float, float)
 	 */
 	@NotNull
 	public final Vector3 invDirection = Vector3.createNew(1.0f / 0.57735f, 1.0f / 0.57735f, 1.0f / 0.57735f);
 
 	/**
 	 * Normalokonstruktor
 	 */
 	private Ray3() {}
 
 	/**
 	 * Setzt Ursprung projectPointhtung
 	 * @param origin Der Ursprung
 	 * @param direction Die Richtung
 	 */
 	private Ray3(@NotNull final Vector3 origin, @NotNull final Vector3 direction) {
 		set(origin, direction);
 	}
 
 	/**
 	 * Setzt den Ursprung des Strahls
 	 * @param origin Der Ursprung
 	 * @return Dieselbe Instanz für method chaining
 	 */
 	@NotNull
 	public Ray3 setOrigin(@NotNull final Vector3 origin) {
 		this.origin.set(origin);
 		return this;
 	}
 
 	/**
 	 * Setzt den Ursprung des Strahls
 	 *
 	 * @param originX Der Ursprung (X-Komponente)
 	 * @param originY Der Ursprung (Y-Komponente)
 	 * @param originZ Der Ursprung (Z-Komponente)
 	 * @return Dieselbe Instanz für method chaining
 	 */
 	@NotNull
 	public Ray3 setOrigin(final float originX, final float originY, final float originZ) {
 		this.origin.set(originX, originY, originZ);
 		return this;
 	}
 
 	/**
 	 * Setzt die Richtung des Strahls
 	 *
 	 * @param direction Die Richtung
 	 * @return Dieselbe Instanz für method chaining
 	 */
 	@NotNull
 	public Ray3 setDirection(@NotNull final Vector3 direction) {
 		return setDirection(direction.x, direction.y, direction.z);
 	}
 
 	/**
 	 * Setzt die Richtung des Strahls
 	 *
 	 * @param directionX Die Richtung (X-Komponente)
 	 * @param directionY Die Richtung (Y-Komponente)
 	 * @param directionZ Die Richtung (Z-Komponente)
 	 * @return Dieselbe Instanz für method chaining
 	 */
 	@NotNull
 	public Ray3 setDirection(final float directionX, final float directionY, final float directionZ) {
 		this.direction.set(directionX, directionY, directionZ).normalize();
 		this.invDirection.set(1.0f / this.direction.x, 1.0f / this.direction.y, 1.0f / this.direction.z);
 		return this;
 	}
 
 	/**
 	 * Kopiert einen Strahl
 	 *
 	 * @param other    Der zu kopierende Ray
 	 * @return Dieselbe Instanz für method chaining
 	 */
 	@NotNull
 	public Ray3 set(@NotNull final Ray3 other) {
 		return set(other.origin, other.direction);
 	}
 
 	/**
 	 * Setzt Ursprung und Richtung des Strahls
 	 * @param origin Der Ursprung
 	 * @param direction Die Richtung
 	 * @return Dieselbe Instanz für method chaining
 	 */
 	@NotNull
 	public Ray3 set(@NotNull final Vector3 origin, @NotNull final Vector3 direction) {
 		setOrigin(origin.x, origin.y, origin.z);
 		setDirection(direction.x, direction.y, direction.z);
 		return this;
 	}
 
 	/**
 	 * Setzt Ursprung und Richtung des Strahls
 	 *
 	 * @param originX Der Ursprung (X-Komponente)
 	 * @param originY Der Ursprung (Y-Komponente)
 	 * @param originZ Der Ursprung (Z-Komponente)
 	 * @param directionX Die Richtung (X-Komponente)
 	 * @param directionY Die Richtung (Y-Komponente)
 	 * @param directionZ Die Richtung (Z-Komponente)
 	 * @return Dieselbe Instanz für method chaining
 	 */
 	@NotNull
 	public Ray3 set(
 			final float originX, final float originY, final float originZ,
 			final float directionX, final float directionY, final float directionZ
 	) {
 		setOrigin(originX, originY, originZ);
 		setDirection(directionX, directionY, directionZ);
 		return this;
 	}
 
 	/**
 	 * Bezieht einen Strahl, der in die entgegengesetzte Richtung zeigt
 	 * @return Der Strahl
 	 */
 	@NotNull @ReturnsCachedValue
 	public Ray3 getInverted() {
 		Vector3 inverted = direction.getInverted();
 		Ray3 ray = Ray3.createNew(origin, inverted);
 		inverted.recycle();
 		return ray;
 	}
 
 	/**
 	 * Dreht den Strahl um, so dass er in die entgegengesetzte Richtung zeigt
 	 *
 	 * @return Diese Instanz für method chaining
 	 */
 	@NotNull
 	public Ray3 invert() {
 		direction.invert();
 		invDirection.set(1.0f/direction.x, 1.0f/direction.y, 1.0f/direction.z);
 		return this;
 	}
 
 	/**
 	 * Berechnet die Distanz eines Punktes zu diesem Strahl
 	 *
 	 * @param point Der Punkt
 	 * @return Die berechnete Distanz
 	 */
 	public float getDistanceFromPoint(@NotNull final Vector3 point) {
 
 		// http://answers.yahoo.com/question/index?qid=20080912194015AAIlm9X
 
 		Vector3 w = point.sub(origin).crossInPlace(direction);
 		float length = w.getLength();
 
 		// aufräumen und raus hier
 		w.recycle();
 		return length;
 	}
 
 	/**
 	 * Projiziert einen Punkt auf den Strahl
 	 *
 	 * @param point Der Punkt
 	 * @return Der projizierte Punkt
 	 */
 	@NotNull @ReturnsCachedValue
 	public Vector3 projectPoint(@NotNull final Vector3 point) {
 		// Richtung bestimmen und auf Richtungsvektor projizieren
 		Vector3 w = point.sub(origin);
 		Vector3 projected = direction.mul(w.dot(direction)); // TODO: Als static herausziehen, damit für diesen Test das Caching der anderen Werte nicht nötig ist
 
 		w.recycle();
 		return projected;
 	}
 
 	/**
 	 * Liefert einen Punkt auf dem Strahl anhand eines Skalars <code>t</code>,
 	 * so dass gilt:
 	 * <p>
 	 * <code>P = t*{@link #direction} + {@link #origin}</code>
 	 * </p>
 	 *
 	 * @param t Der Skalar
 	 * @return Der Punkt auf dem Strahl
 	 */
 	@ReturnsCachedValue @NotNull
 	public Vector3 getPoint(final float t){
 		return direction.mul(t).addInPlace(origin);
 	}
 
 	/**
 	 * Transformiert diesen Strahl
 	 *
 	 * @param transformation Die Transformationsmatrix
 	 * @return Diese Instanz für method chaining
 	 */
 	@NotNull
 	public Ray3 transform(@NotNull final Matrix4 transformation) {
 		transformation.transformPointInPlace(origin);
 		transformation.transformVectorInPlace(direction);
 		return this;
 	}
 }
