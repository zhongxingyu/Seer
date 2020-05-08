 
 package microfont.render;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import microfont.AbstractPixselMap;
 import microfont.AbstractPixselMap.PixselIterator;
 import microfont.PixselMap;
 import microfont.events.PixselMapEvent;
 import microfont.events.PixselMapListener;
 
 /**
  * Класс для отрисовки карты пикселей.
  */
 public class PixselMapRender implements ColorIndex, StylePropertyName {
     /** Карта пикселей для отрисовки. */
     private AbstractPixselMap pixmap;
     /** Интерфейс для отправки запросов о перерисовки и изменении размеров. */
     private ComponentRequest  req;
     /** Получатель сообщений от карты пикселей. */
     private PixselListener    listener;
     /** Высота карты пикселей как картинки. */
     private int               width;
     /** Ширина карты пикселей как картинки. */
     private int               height;
     /** Набор цветов для отрисовки пикселей. */
     private Color[]           colors;
     /** Ширина пикселя. */
     private int               pixselWidth;
     /** Высота пикселя. */
     private int               pixselHeight;
     /** Соотношение высоты пикселя к его ширине. */
     private float             pixselRatio;
     /** Зазор между пикселями. */
     private int               space;
     private int               stepX;
     private int               stepY;
     /** Отрисовывать только закрашенные пиксели. */
     private boolean           drawOnlyInk;
     /** Толщина сетки. */
     private int               gridThickness;
     /** Размер сетки. */
     private int               gridSize;
     /** Отрисовывать сетку. */
     private boolean           drawGrid;
     /** Размер левого поля. */
     private int               marginLeft;
     /** Размер правого поля. */
     private int               marginRight;
     /** Расстояние до базовой линии строки. */
     private int               base;
     /** Высота строчных букв. */
     private int               line;
     /** Подъём (прописных) букв. */
     private int               ascent;
     /** Понижение букв. */
     private int               descent;
     /** Отрисовывать поля своими цветами. */
     private boolean           drawMargins;
 
     /**
      * Создание отрисовщика.
      * 
      * @param compReq
      */
     public PixselMapRender(ComponentRequest compReq) {
         req = compReq;
         listener = new PixselListener();
         colors = new Color[COLOR_MAX + 1];
         pixselWidth = 1;
         pixselRatio = 1.0f;
         space = 0;
     }
 
     /**
      * Создание отрисовщика.
      */
     public PixselMapRender() {
         this(null);
     }
 
     /**
      * Отрисовка карты пикселей.
      * 
      * @param g Графический контекст для отрисовки.
      * @param x Начальная координата по горизонтали.
      * @param y Начальная координата по вертикали.
      */
     public void paint(Graphics g, int x, int y) {
         if (pixmap == null || pixmap.isEmpty()) return;
 
         int startX = 0;
         int startY = 0;
         int width = this.width;
         int height = this.height;
         Color defCol = g.getColor();
         // Коррекция области отрисовки с учётом ограничения контекстом.
         Rectangle clip = g.getClipBounds();
         if (clip != null) {
             if (x < clip.x) {
                 startX = clip.x - x;
                 width -= startX;
             }
 
             width = width < clip.width ? width : clip.width;
 
             if (y < clip.y) {
                 startY = clip.y - y;
                 height -= startY;
             }
 
             height = height < clip.height ? height : clip.height;
 
             if (height <= 0 || height <= 0) return;
         }
 
         int pixselCountX;
         int pixselCountY;
         int renderStartX = startX;
         int renderStartY = startY;
 
         // Коррекция области отрисовки по границам краёв пикселей.
         pixselCountX = width + renderStartX;
         pixselCountX = pointToPixselX(pixselCountX + stepX - 1);
         renderStartX = pointToPixselX(renderStartX);
         pixselCountX -= renderStartX;
         pixselCountY = height + renderStartY;
         pixselCountY = pointToPixselY(pixselCountY + stepY - 1);
         renderStartY = pointToPixselY(renderStartY);
         pixselCountY -= renderStartY;
 
         PixselIterator pi = pixmap
                         .getIterator(renderStartX, renderStartY, pixselCountX,
                                         pixselCountY,
                                         PixselIterator.DIR_TOP_LEFT);
 
         renderStartX = pixselToPointX(renderStartX) + x;
         renderStartY = pixselToPointY(renderStartY) + y;
 
         int posX = renderStartX;
         int posY;
         for (int i = 0; i < pixselCountX; i++) {
             posY = renderStartY;
             for (int j = 0; j < pixselCountY; j++) {
                 drawPixsel(g, posX, posY,
                                 indexAt(pi.getX(), pi.getY(), pi.getNext()),
                                 defCol);
                 posY += stepY;
             }
             posX += stepX;
         }
 
         if (!drawOnlyInk) {
             if (pixselCountX <= 0 && pixselCountY <= 0) return;
 
             Color c = colorAt(COLOR_SPACE, defCol);
             if (c != null && space != 0) {
                 g.setColor(c);
 
                 posX = renderStartX + pixselWidth;
                 for (int i = 0; i < pixselCountX - 1; i++) {
                     g.fillRect(posX, startY + y, space, height);
                     posX += stepX;
                 }
 
                 posY = renderStartY + pixselHeight;
                 for (int i = 0; i < pixselCountY - 1; i++) {
                     g.fillRect(startX + x, posY, width, space);
                     posY += stepY;
                 }
             }
 
             c = colorAt(COLOR_GRID, defCol);
             if (c != null && gridThickness != 0) {
                 g.setColor(c);
 
                 posX = renderStartX;
                 for (int i = 0; i < pixselCountX + 1; i++) {
                     posY = renderStartY;
                     for (int j = 0; j < pixselCountY + 1; j++) {
                         drawGrid(g, posX, posY, c);
                         posY += stepY;
                     }
                     posX += stepX;
                 }
             }
         }
     }
 
     /**
      * Возвращает ширину картинки, отрисовываемой рендером.
      */
     public int getWidth() {
         return width;
     }
 
     /**
      * Возвращает высоту картинки, отрисовываемой рендером.
      */
     public int getHeight() {
         return height;
     }
 
     /**
      * Возвращает отображаемую карту пикселей.
      */
     public AbstractPixselMap getPixselMap() {
         return pixmap;
     }
 
     /**
      * Устанавливает отображаемую карту пикселей. Если карта является
      * {@link PixselMap}, то к ней подключается получатель сообщений об
      * изменениях размеров или пикселей. Это сообщения трансформируются в
      * запросы о перерисовке или изменении размеров.
      * 
      * @param pm Отображаемая карта.
      */
     public void setPixselMap(AbstractPixselMap pm) {
         AbstractPixselMap old = pixmap;
         if (old instanceof PixselMap) {
             ((PixselMap) old).removePixselMapListener(listener);
             ((PixselMap) old).removePropertyChangeListener(listener);
         }
 
         pixmap = pm;
         if (pixmap instanceof PixselMap) {
             ((PixselMap) pixmap).addPixselMapListener(listener);
             ((PixselMap) pixmap).addPropertyChangeListener(listener);
         }
 
         updateSize();
         requestRepaint();
     }
 
     /**
      * Возвращает интерфейс для запросов при измении картинки.
      */
     public ComponentRequest getComponent() {
         return req;
     }
 
     /**
      * Устанавливает интерфейс для запросов при изменении картинки.
      * 
      * @param compReq Новый интерфейс для запросов.
      */
     public void setComponent(ComponentRequest compReq) {
         req = compReq;
         // XXX Возможно, эти действия и не нужны.
         requestInvalidate();
         requestRepaint();
     }
 
     /**
      * Возвращает цвет по индексу.
      * 
      * @param cInd Индекс цвета. Может быть одним из<br>
      *            {@link ColorIndex#COLOR_PAPER}<br>
      *            {@link ColorIndex#COLOR_INK}<br>
      *            {@link ColorIndex#COLOR_PAPER_MARGINS}<br>
      *            {@link ColorIndex#COLOR_INK_MARGINS}<br>
      *            {@link ColorIndex#COLOR_PAPER_ASCENT}<br>
      *            {@link ColorIndex#COLOR_INK_ASCENT}<br>
      *            {@link ColorIndex#COLOR_PAPER_DESCENT}<br>
      *            {@link ColorIndex#COLOR_INK_DESCENT}<br>
      *            {@link ColorIndex#COLOR_SPACE}<br>
      *            {@link ColorIndex#COLOR_GRID}
      */
     public Color getColor(int cInd) {
         if (cInd < 0 || cInd > COLOR_MAX) return null;
         return colors[cInd];
     }
 
     /**
      * Устанавливает цвет по индексу.
      * 
      * @param cInd Индекс цвета. Может быть одним из<br>
      *            {@link ColorIndex#COLOR_PAPER}<br>
      *            {@link ColorIndex#COLOR_INK}<br>
      *            {@link ColorIndex#COLOR_PAPER_MARGINS}<br>
      *            {@link ColorIndex#COLOR_INK_MARGINS}<br>
      *            {@link ColorIndex#COLOR_PAPER_ASCENT}<br>
      *            {@link ColorIndex#COLOR_INK_ASCENT}<br>
      *            {@link ColorIndex#COLOR_PAPER_DESCENT}<br>
      *            {@link ColorIndex#COLOR_INK_DESCENT}<br>
      *            {@link ColorIndex#COLOR_SPACE}<br>
      *            {@link ColorIndex#COLOR_GRID}
      * @param c Устанавливаемый цвет.
      */
     public void setColor(int cInd, Color c) {
         if (cInd < 0 || cInd > COLOR_MAX) return;
 
         Color old = colors[cInd];
         colors[cInd] = c;
 
         if (pixmap == null) return;
         if (old == null) {
             if (c == null) return;
         } else if (old.equals(c)) return;
         // XXX Нужна проверка, используется ли цвет при отрисовке и в каком
         // месте.
         requestRepaint();
     }
 
     /**
      * Возвращает размер пикселя.
      */
     public int getPixselSize() {
         return pixselWidth;
     }
 
     /**
      * Устанавливает размер пикселя.
      * 
      * @param size Новый размер пикселя.
      * @throws IllegalArgumentException Если {@code size} меньше или равно нулю.
      */
     public void setPixselSize(int size) {
         if (size <= 0)
             throw new IllegalArgumentException("pixsel size =" + size);
 
         int old = pixselWidth;
         pixselWidth = size;
 
         updateSize();
         if (old != pixselWidth && pixmap != null) requestRepaint();
     }
 
     /**
      * Возвращает соотношение ширины пикселя к его высоте.
      */
     public float getPixselRatio() {
         return pixselRatio;
     }
 
     /**
      * Устанавливает соотношение ширины пикселя к его высоте.
      * 
      * @param ratio соотношение ширины пикселя к его высоте.
      * @throws IllegalArgumentException Если {@code ratio} меньше или равно
      *             нулю.
      */
     public void setPixselRatio(float ratio) {
         if (ratio <= 0f)
             throw new IllegalArgumentException("pixsel ratio =" + ratio);
 
         float old = pixselRatio;
         pixselRatio = ratio;
 
         updateSize();
         if (old != pixselRatio && pixmap != null) requestRepaint();
     }
 
     /**
      * Возврашает величину зазора между пикселями.
      */
     public int getSpace() {
         return space;
     }
 
     /**
      * Устанавливает величину зазора между пикселями.
      * 
      * @param sp Зазор между пикселями.
      * @throws IllegalArgumentException Если {@code ratio} меньше нуля.
      */
     public void setSpace(int sp) {
         if (sp < 0) throw new IllegalArgumentException("space =" + sp);
 
         int old = space;
         space = sp;
 
         updateSize();
         if (old != space && pixmap != null) requestRepaint();
     }
 
     /**
      * Возвращает толщину сетки.
      */
     public int getGridThickness() {
         return gridThickness;
     }
 
     /**
      * Устанавливает толщину сетки.
      * 
      * @param gt Толщина сетки.
      * @throws IllegalArgumentException Если {@code gt} меньше нуля.
      */
     public void setGridThickness(int gt) {
         if (gt < 0)
             throw new IllegalArgumentException("grid thickness =" + gt);
 
         int old = gridThickness;
         gridThickness = gt;
 
         if (old != gridThickness && drawGrid && pixmap != null)
             requestRepaint();
     }
 
     /**
      * Возвращает размер сетки.
      */
     public int getGridSize() {
         return gridSize;
     }
 
     /**
      * Устанавливает размер сетки.
      * 
      * @param gs Размер сетки.
      * @throws IllegalArgumentException Если {@code gs} меньше нуля.
      */
     public void setGridSize(int gs) {
         if (gs < 0) throw new IllegalArgumentException("grid size =" + gs);
 
         int old = gridSize;
         gridSize = gs;
 
         if (old != gridSize && drawGrid && pixmap != null) requestRepaint();
     }
 
     /**
      * Возвращает величину левого поля.
      */
     public int getMarginLeft() {
         return marginLeft;
     }
 
     /**
      * Устанавливает величину левого поля.
      * 
      * @param m Левое поле.
      * @throws IllegalArgumentException Если {@code m} меньше нуля.
      */
     public void setMarginLeft(int m) {
         if (m < 0) throw new IllegalArgumentException("left margin =" + m);
 
         int old = marginLeft;
         marginLeft = m;
 
         if (old != marginLeft && drawMargins && pixmap != null) {
             int w = old < marginLeft ? marginLeft : old;
             Rectangle rect = new Rectangle(0, 0, w, pixmap.getHeight());
             rect = toPointRect(rect, rect);
             requestRepaint(rect);
         }
     }
 
     /**
      * Возвращает величину правого поля.
      */
     public int getMarginRight() {
         return marginRight;
     }
 
     /**
      * Устанавливает величину правого поля.
      * 
      * @param m Правое поле.
      * @throws IllegalArgumentException Если {@code m} меньше нуля.
      */
     public void setMarginRight(int m) {
         if (m < 0) throw new IllegalArgumentException("right margin =" + m);
 
         int old = marginRight;
         marginRight = m;
 
         if (old != marginRight && drawMargins && pixmap != null) {
             int w = old < marginRight ? marginRight : old;
            Rectangle rect = new Rectangle(0, pixmap.getWidth() - w, w,
                             pixmap.getHeight());
             rect = toPointRect(rect, rect);
             requestRepaint(rect);
         }
     }
 
     /**
      * Возвращает величину базовой линии символа.
      */
     public int getBase() {
         return base;
     }
 
     /**
      * Устанавливает величину базовой линии символа.
      * 
      * @param b Базовая линия символа.
      * @throws IllegalArgumentException Если {@code b} меньше нуля.
      */
     public void setBase(int b) {
         if (b < 0) throw new IllegalArgumentException("baseline =" + b);
 
         int old = base;
         base = b;
 
         if (old != base && pixmap != null) {
             requestInvalidate();
             // XXX calculate region
             if (drawMargins) requestRepaint();
         }
     }
 
     /**
      * Возвращает высоту линии символа.
      */
     public int getLine() {
         return line;
     }
 
     /**
      * Устанавливает высоту линии символа.
      * 
      * @param l Высота линии символа.
      * @throws IllegalArgumentException Если {@code l} меньше нуля.
      */
     public void setLine(int l) {
         if (l < 0) throw new IllegalArgumentException("line =" + l);
 
         int old = line;
         line = l;
 
         if (old != line && drawMargins && pixmap != null) {
             // XXX calculate region
             requestRepaint();
         }
     }
 
     /**
      * Возвращает подъём символа.
      */
     public int getAscent() {
         return ascent;
     }
 
     /**
      * Устанавливает подъём символа.
      * 
      * @param a Подъём символа.
      * @throws IllegalArgumentException Если {@code a} меньше нуля.
      */
     public void setAscent(int a) {
         if (a < 0) throw new IllegalArgumentException("ascent =" + a);
 
         int old = ascent;
         ascent = a;
 
         if (old != ascent && drawMargins && pixmap != null) {
             // XXX calculate region
             requestRepaint();
         }
     }
 
     /**
      * Возвращает величину подстрочной области символа.
      */
     public int getDescent() {
         return descent;
     }
 
     /**
      * Устанавливает величину подстрочной части символа.
      * 
      * @param d Подстрочная часть символа.
      * @throws IllegalArgumentException Если {@code d} меньше нуля.
      */
     public void setDescent(int d) {
         if (d < 0) throw new IllegalArgumentException("descent =" + d);
 
         int old = descent;
         descent = d;
 
         if (old != descent && drawMargins && pixmap != null) {
             // XXX calculate region
             requestRepaint();
         }
     }
 
     /**
      * Возвращает {@code true} если отрисовываются только закрашенные пиксели.
      */
     public boolean isDrawOnlyInk() {
         return drawOnlyInk;
     }
 
     /**
      * Устанавливает режим отрисовки символов.
      * 
      * @param doi {@code true} если нужно отрисовывать только закрашенные
      *            пиксели.
      */
     public void setDrawOnlyInk(boolean doi) {
         boolean old = drawOnlyInk;
         drawOnlyInk = doi;
 
         if (old != drawOnlyInk && pixmap != null) requestRepaint();
     }
 
     /**
      * Возвращает {@code true} если отрисовывается сетка.
      */
     public boolean isDrawGrid() {
         return drawGrid;
     }
 
     /**
      * Устанавливает отрисовку сетки.
      * 
      * @param dg {@code true} если нужно отрисовывать сетку.
      */
     public void setDrawGrid(boolean dg) {
         boolean old = drawGrid;
         drawGrid = dg;
 
         if (old != drawGrid && pixmap != null) requestRepaint();
     }
 
     /**
      * Возвращает {@code true} если поля отрисовываются.
      */
     public boolean isDrawMargins() {
         return drawMargins;
     }
 
     /**
      * Устанавливает отрисовку полей.
      * 
      * @param dm {@code true} если нужно отрисовывать поля.
      */
     public void setDrawMargins(boolean dm) {
         boolean old = drawMargins;
         drawMargins = dm;
 
         if (old != drawMargins && pixmap != null) requestRepaint();
     }
 
     /**
      * Возвращает информацию о точке изображения как о части карты пикселей.
      * 
      * @param info Объект для сохранения информации. Может быть {@code null}; в
      *            этом случае метод вернёт новый объект.
      * @param x Горизонтальная позиция точки изображения.
      * @param y Вертикальная позиция точки изображения.
      * @return {@code info} или, если {@code info} был {@code null}, новый
      *         объект с информацией о точке изображения.
      */
     public PointInfo getPointInfo(PointInfo info, int x, int y) {
         PointInfo ret;
         if (info == null) ret = new PointInfo();
         else ret = info;
 
         ret.x = x / stepX;
         ret.y = y / stepY;
 
         int px = x % stepX;
         int py = y % stepY;
 
         if (px > pixselWidth || py > pixselHeight) {
             ret.space = true;
             ret.pixsel = false;
         } else {
             ret.space = false;
             ret.pixsel = true;
         }
 
         if (x < 0 || y < 0 || x > width || y > height) {
             ret.pixsel = false;
             ret.space = false;
         }
 
         // XXX calculate dead zone is not complete!!!
         ret.deadZone = false;
         if (ret.pixsel) {
             if (px > pixselWidth / 2) px = pixselWidth - px;
             else px++;
 
             if (py > pixselHeight / 2) py = pixselHeight - py;
             else py++;
 
             float hit = (float) px * py / (pixselWidth * pixselHeight);
             if (hit <= (float) 1 / 16) ret.deadZone = true;
         }
 
         return ret;
     }
 
     /**
      * Преобразует горизонтальную координату карты пикселей в координату
      * изображения. Координата изображения берётся для левого края указанного
      * пикселя.
      * 
      * @param x Горизонтальная координата карты пикселей.
      * @return Горизонтальная координата изображения.
      */
     public int pixselToPointX(int x) {
         return x * stepX;
     }
 
     /**
      * Преобразует вертикальную координату карты пикселей в координату
      * изображения. Координата изображения берётся для верхнего края указанного
      * пикселя.
      * 
      * @param y Вертикальная координата карты пикселей.
      * @return Вертикальная координата изображения.
      */
     public int pixselToPointY(int y) {
         return y * stepY;
     }
 
     /**
      * Преобразует горизонтальную координату изображения в координату карты
      * пикселей. Координата карты пикселей соответствует пикселю, которому
      * принадлежит точка изображения или пикселю левее зазора, если точка
      * изображения приходится на зазор между пикселями.
      * 
      * @param x Горизонтальная координата изображения.
      * @return Горизонтальная координата карты пикселей.
      */
     public int pointToPixselX(int x) {
         return x / stepX;
     }
 
     /**
      * Преобразует вертикальную координату изображения в координату карты
      * пикселей. Координата карты пикселей соответствует пикселю, которому
      * принадлежит точка изображения или пикселю выше зазора, если точка
      * изображения приходится на зазор между пикселями.
      * 
      * @param y Вертикальная координата изображения.
      * @return Вертикальная координата карты пикселей.
      */
     public int pointToPixselY(int y) {
         return y / stepY;
     }
 
     /**
      * Преобразует координаты прямоугольника изображения в координаты карты
      * пикселей.
      * 
      * @param points Координаты прямоугольника изображения.
      * @param pixsels Объект для результата преобразования. Может быть
      *            {@code null}, в этом случае создаётся и возвращается новый
      *            объект. Так же может быть {@code points}, если координаты
      *            изображения в дальнейшем не нужны.
      * @return {@code pixsels} или новый объект с результатом преобразования.
      * @throws NullPointerException Если {@code points} равен {@code null}.
      */
     public Rectangle toPixselRect(Rectangle points, Rectangle pixsels) {
         if (points == null) throw new NullPointerException("points is null");
 
         Rectangle ret;
         if (pixsels == null) ret = new Rectangle();
         else ret = pixsels;
 
         ret.width = pointToPixselX(points.width + points.x + pixselWidth
                         + space - 1);
         ret.height = pointToPixselY(points.height + points.y + pixselHeight
                         + space - 1);
         ret.x = pointToPixselX(points.x);
         ret.y = pointToPixselY(points.y);
         ret.width -= ret.x;
         ret.height -= ret.y;
 
         return ret;
     }
 
     /**
      * Преобразует координаты прямоугольника карты пикселей в координаты
      * изображения.
      * 
      * @param pixsels Координаты прямоугольника карты пикселей.
      * @param points Объект для результата преобразования. Может быть
      *            {@code null}, в этом случае создаётся и возвращается новый
      *            объект. Так же может быть {@code pixsels}, если координаты
      *            карты пикселей в дальнейшем не нужны.
      * @return {@code points} или новый объект с результатом преобразования.
      * @throws NullPointerException Если {@code pixsels} равен {@code null}.
      */
     public Rectangle toPointRect(Rectangle pixsels, Rectangle points) {
         if (pixsels == null) throw new NullPointerException("pixsels is null");
 
         Rectangle ret;
         if (points == null) ret = new Rectangle();
         else ret = points;
 
         ret.x = pixselToPointX(pixsels.x);
         ret.y = pixselToPointY(pixsels.y);
 
        ret.width = pixselToPointX(pixsels.width);
        ret.height = pixselToPointY(pixsels.height);
 
         return ret;
     }
 
     /**
      * Обновляет размеры изображения. При необходимости делает запрос о
      * изменении размеров.
      */
     protected void updateSize() {
         int oldW = width;
         int oldH = height;
 
         // Округлённое значение высоты.
         pixselHeight = (int) (pixselWidth * 2 * pixselRatio) / 2;
         if (pixselHeight < 1) pixselHeight = 1;
         stepX = pixselWidth + space;
         stepY = pixselHeight + space;
         width = 0;
         height = 0;
         if (pixmap != null) {
             int c = pixmap.getWidth();
             int r = pixmap.getHeight();
             if (c != 0) width = pixselToPointX(c) - space;
             if (r != 0) height = pixselToPointY(r) - space;
         }
 
         if (oldW != width || oldH != height) requestInvalidate();
     }
 
     /**
      * Возвращает индекс цвета для заданного пикселя.
      * 
      * @param x Горизонтальная позиция пикселя.
      * @param y Вертикальная позиция пикселя.
      * @param ink {@code true} если пиксель должен быть закрашен.
      * 
      * @return Индекс цвета.
      */
     protected int indexAt(int x, int y, boolean ink) {
         if (pixmap == null) return COLOR_PAPER;
 
         if (drawMargins) {
             if (x < marginLeft || pixmap.getWidth() - x <= marginRight)
                 return ink ? COLOR_INK_MARGINS : COLOR_PAPER_MARGINS;
             if (y < base - ascent || y >= base + descent)
                 return ink ? COLOR_INK_MARGINS : COLOR_PAPER_MARGINS;
             if (y < base - line)
                 return ink ? COLOR_INK_ASCENT : COLOR_PAPER_ASCENT;
             if (y >= base)
                 return ink ? COLOR_INK_DESCENT : COLOR_PAPER_DESCENT;
         }
 
         // Значения по умолчанию.
         return ink ? COLOR_INK : COLOR_PAPER;
     }
 
     /**
      * Возвращает цвет по индексу.
      * 
      * @param index Индекс цвета.
      * @param fg Цвет рисования по умолчанию.
      * @return Цвет для заданного индекса. Для индексов, соответствующих бумаге,
      *         сетке или зазору возможен возврат {@code null}
      */
     protected Color colorAt(int index, Color fg) {
         Color ret;
 
         switch (index) {
         case COLOR_INK_DESCENT:
             ret = colors[COLOR_INK_DESCENT];
             if (ret != null) break;
         case COLOR_INK_ASCENT:
             ret = colors[COLOR_INK_ASCENT];
             if (ret != null) break;
         case COLOR_INK:
             ret = colors[COLOR_INK];
             if (ret == null) ret = fg;
             break;
         case COLOR_INK_MARGINS:
             ret = colors[COLOR_INK_MARGINS];
             if (ret == null) ret = colors[COLOR_INK];
             if (ret == null) ret = fg;
             break;
         case COLOR_PAPER_DESCENT:
             ret = colors[COLOR_PAPER_DESCENT];
             if (ret != null) break;
         case COLOR_PAPER_ASCENT:
             ret = colors[COLOR_PAPER_ASCENT];
             if (ret != null) break;
         case COLOR_PAPER:
             ret = colors[COLOR_PAPER];
             break;
         case COLOR_PAPER_MARGINS:
             ret = colors[COLOR_PAPER_MARGINS];
             if (ret == null) ret = colors[COLOR_PAPER];
             break;
         case COLOR_GRID:
             ret = colors[COLOR_GRID];
             if (ret != null) break;
         case COLOR_SPACE:
             ret = colors[COLOR_SPACE];
             if (ret != null) break;
         default:
             ret = null;
         }
 
         return ret;
     }
 
     /**
      * Отрисовка пикселя.
      * 
      * @param g Графический контекст отрисовки.
      * @param x Горизонтальная координата начала отрисовки в точках изображения.
      * @param y Вертикальная координата начала отрисовки в точках изображения.
      * @param cInd Индекс цвета пикселя.
      * @param fg Цвет чернил по умолчанию.
      */
     protected void drawPixsel(Graphics g, int x, int y, int cInd, Color fg) {
         Color c = colorAt(cInd, fg);
         if (c == null) {
             if (!drawOnlyInk) g.clearRect(x, y, pixselWidth, pixselHeight);
         } else {
             g.setColor(c);
             g.fillRect(x, y, pixselWidth, pixselHeight);
         }
     }
 
     /**
      * Отрисовка одного перекрестия сетки. Координаты начала отрисовки
      * соответствуют центру сетки.
      * 
      * @param g Графический контекст.
      * @param x Горизонтальная координата центра перекрестия.
      * @param y Вертикальная координата центра перекрестия.
      * @param c Цвет сетки.
      */
     protected void drawGrid(Graphics g, int x, int y, Color c) {
        int halfT = ( space + gridThickness) / 2;
        int halfS = ( space + gridSize) / 2;
 
         g.fillRect(x - halfT, y - halfS, gridThickness, gridSize);
         g.fillRect(x - halfS, y - halfT, gridSize, gridThickness);
     }
 
     /**
      * Запрос на перерисовку части картинки.
      * 
      * @param rect Область перерисовки. Отсчёт в экранных точках.
      */
     protected void requestRepaint(Rectangle rect) {
         if (req != null) req.requestRepaint(rect);
     }
 
     /**
      * Запрос на перерисовку всей картинки.
      */
     protected void requestRepaint() {
         if (req != null) req.requestRepaint();
     }
 
     /**
      * Запрос при изменении размеров.
      */
     protected void requestInvalidate() {
         if (req != null) req.requestInvalidate();
     }
 
     /**
      * Класс для получения сообщений от карты пикселей.
      */
     private class PixselListener implements PixselMapListener,
                     PropertyChangeListener {
         Rectangle rect = new Rectangle();
 
         @Override
         public void propertyChange(PropertyChangeEvent evt) {
             if (!evt.getPropertyName().equals(PixselMap.PROPERTY_SIZE)) return;
             requestInvalidate();
             requestRepaint();
         }
 
         @Override
         public void pixselChanged(PixselMapEvent event) {
             rect.setBounds(event.x(), event.y(), event.width(), event.height());
             rect = toPointRect(rect, rect);
             requestRepaint(rect);
         }
     }
 }
